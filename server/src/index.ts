import { createServer } from "node:http";
import { buildSearchResponse } from "./engine.js";
import { attachBestFlexibleDate, shiftedRequest } from "./flexibility.js";
import { verificationResponse } from "./verify.js";
import { BookingDemandProvider } from "./providers/booking.js";
import { ExpediaRapidProvider } from "./providers/expedia.js";
import { AmadeusHotelProvider } from "./providers/amadeus.js";
import { BraveOfferDiscovery } from "./providers/brave.js";
import { TavilyOfferDiscovery } from "./providers/tavily.js";
import { GooglePlacesReviewProvider } from "./providers/googlePlacesReviews.js";
import type { DiscoveryLead, ExternalReviewRecord, SearchRequest, SearchResponse, VerifyRequest } from "./types.js";

const providers = [new BookingDemandProvider(), new ExpediaRapidProvider(), new AmadeusHotelProvider()];
const discoveryProviders = [new BraveOfferDiscovery(), new TavilyOfferDiscovery()];
const reviewProviders = [new GooglePlacesReviewProvider()];

function json(res: any, code: number, body: unknown) {
  res.writeHead(code, {"Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store", "Access-Control-Allow-Origin": "*"});
  res.end(JSON.stringify(body));
}
async function body(req: any): Promise<any> { let raw = ""; for await (const chunk of req) raw += chunk; return raw ? JSON.parse(raw) : {}; }
function validDate(v: string) { return /^\d{4}-\d{2}-\d{2}$/.test(v) && Number.isFinite(new Date(`${v}T00:00:00Z`).getTime()); }
function validate(r: SearchRequest): string | null {
  if (!r.city?.trim()) return "city is required";
  if (!validDate(r.checkIn) || !validDate(r.checkOut)) return "checkIn/checkOut must use YYYY-MM-DD";
  if (new Date(r.checkOut) <= new Date(r.checkIn)) return "checkOut must be after checkIn";
  if (!(r.adults >= 1 && r.adults <= 30)) return "adults out of range";
  if (!(r.rooms >= 1 && r.rooms <= 8)) return "rooms out of range";
  if ((r.flexibilityDays ?? 0) < 0 || (r.flexibilityDays ?? 0) > 3) return "flexibilityDays must be between 0 and 3";
  return null;
}
async function supplierSearch(request: SearchRequest, onlyProvider?: string): Promise<SearchResponse> {
  const wanted = (onlyProvider || "").toLowerCase();
  const selected = !wanted ? providers : providers.filter(p => {
    const n = p.name.toLowerCase();
    if (wanted.includes("booking")) return n.includes("booking");
    if (wanted.includes("expedia")) return n.includes("expedia");
    if (wanted.includes("amadeus")) return n.includes("amadeus");
    return n.includes(wanted) || wanted.includes(n);
  });
  const settled = await Promise.all(selected.map(p => p.search(request)));
  return buildSearchResponse(request, settled, []);
}
async function flexibleSearches(request: SearchRequest): Promise<SearchResponse[]> {
  const days = Math.min(3, Math.max(0, Math.trunc(request.flexibilityDays ?? 0)));
  if (!days) return [];
  const offsets: number[] = []; for (let n = 1; n <= days; n++) offsets.push(-n, n);
  const output: SearchResponse[] = [];
  for (let i = 0; i < offsets.length; i += 2) {
    const results = await Promise.all(offsets.slice(i, i + 2).map(offset => supplierSearch(shiftedRequest(request, offset))));
    output.push(...results);
  }
  return output;
}
async function discoverOffers(request: SearchRequest, hotelNames: string[]): Promise<DiscoveryLead[]> {
  const seen = new Set<string>();
  const output: DiscoveryLead[] = [];
  for (const discovery of discoveryProviders) {
    if (!discovery.isConfigured()) continue;
    if (output.length >= 25) break;
    try {
      const leads = await discovery.discover(request, hotelNames);
      for (const lead of leads) {
        const key = lead.url.trim().toLowerCase();
        if (!key || seen.has(key)) continue;
        seen.add(key);
        output.push(lead);
        if (output.length >= 100) break;
      }
    } catch { }
  }
  return output;
}
async function fetchExternalReviews(hotelNames: string[], city: string): Promise<{reviews: ExternalReviewRecord[]; configured: number}> {
  const configuredProviders = reviewProviders.filter(provider => provider.isConfigured());
  if (!configuredProviders.length || !hotelNames.length) return {reviews: [], configured: configuredProviders.length};
  const settled = await Promise.allSettled(configuredProviders.map(provider => provider.fetch(hotelNames, city)));
  const reviews: ExternalReviewRecord[] = [];
  for (const result of settled) {
    if (result.status === "fulfilled") reviews.push(...result.value.reviews);
  }
  return {reviews, configured: configuredProviders.length};
}

const server = createServer(async (req, res) => {
  try {
    if (req.method === "OPTIONS") return json(res, 204, {});
    if (req.method === "GET" && req.url === "/health") return json(res, 200, {
      ok: true,
      service: "iqtans-engine",
      time: new Date().toISOString(),
      providers: providers.map(p => ({name: p.name, configured: p.isConfigured()})),
      discovery: discoveryProviders.map(p => ({name: p.name, configured: p.isConfigured()})),
      reviews: reviewProviders.map(p => ({name: p.name, configured: p.isConfigured()}))
    });
    if (req.method === "POST" && req.url === "/v1/search") {
      const request = await body(req) as SearchRequest;
      const problem = validate(request); if (problem) return json(res, 400, {error: problem});
      request.bookerCountry ||= "sa"; request.currency ||= "SAR";
      const exactSettled = await Promise.all(providers.map(p => p.search(request)));
      const names = [...new Set(exactSettled.flatMap(r => r.offers.map(o => o.hotelName)))];
      const [leads, alternatives, external] = await Promise.all([
        discoverOffers(request, names),
        flexibleSearches(request),
        fetchExternalReviews(names, request.city)
      ]);
      const exact = buildSearchResponse(request, exactSettled, leads, external.reviews, external.configured);
      return json(res, 200, attachBestFlexibleDate(exact, alternatives));
    }
    if (req.method === "POST" && req.url === "/v1/verify") {
      const verify = await body(req) as VerifyRequest;
      const problem = validate(verify.search); if (problem) return json(res, 400, {error: problem});
      if (!verify.provider?.trim() || !verify.providerHotelId?.trim() || !(verify.expectedPrice > 0)) return json(res, 400, {error: "provider, providerHotelId and expectedPrice are required"});
      verify.search.bookerCountry ||= "sa"; verify.search.currency ||= "SAR"; verify.search.flexibilityDays = 0;
      const fresh = await supplierSearch(verify.search, verify.provider);
      return json(res, 200, verificationResponse(verify, fresh));
    }
    return json(res, 404, {error: "not found"});
  } catch (error) { return json(res, 500, {error: error instanceof Error ? error.message : String(error)}); }
});
const port = Number(process.env.PORT || 8787);
server.listen(port, () => console.log(`iqtans-engine listening on ${port}`));
