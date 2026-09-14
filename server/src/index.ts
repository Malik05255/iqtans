import { createServer } from "node:http";
import { buildSearchResponse } from "./engine.js";
import { BookingDemandProvider } from "./providers/booking.js";
import { ExpediaRapidProvider } from "./providers/expedia.js";
import { BraveOfferDiscovery } from "./providers/brave.js";
import type { SearchRequest } from "./types.js";

const providers = [new BookingDemandProvider(), new ExpediaRapidProvider()];
const discovery = new BraveOfferDiscovery();

function json(res: any, code: number, body: unknown) {
  res.writeHead(code, {"Content-Type": "application/json; charset=utf-8", "Cache-Control": "no-store", "Access-Control-Allow-Origin": "*"});
  res.end(JSON.stringify(body));
}
async function body(req: any): Promise<any> {
  let raw = "";
  for await (const chunk of req) raw += chunk;
  return raw ? JSON.parse(raw) : {};
}
function validDate(v: string) { return /^\d{4}-\d{2}-\d{2}$/.test(v) && Number.isFinite(new Date(`${v}T00:00:00Z`).getTime()); }
function validate(r: SearchRequest): string | null {
  if (!r.city?.trim()) return "city is required";
  if (!validDate(r.checkIn) || !validDate(r.checkOut)) return "checkIn/checkOut must use YYYY-MM-DD";
  if (new Date(r.checkOut) <= new Date(r.checkIn)) return "checkOut must be after checkIn";
  if (!(r.adults >= 1 && r.adults <= 30)) return "adults out of range";
  if (!(r.rooms >= 1 && r.rooms <= 8)) return "rooms out of range";
  return null;
}

const server = createServer(async (req, res) => {
  try {
    if (req.method === "OPTIONS") return json(res, 204, {});
    if (req.method === "GET" && req.url === "/health") return json(res, 200, {
      ok: true, service: "iqtans-engine", time: new Date().toISOString(),
      providers: providers.map(p => ({name: p.name, configured: p.isConfigured()})),
      discovery: {name: discovery.name, configured: discovery.isConfigured()}
    });
    if (req.method === "POST" && req.url === "/v1/search") {
      const request = await body(req) as SearchRequest;
      const problem = validate(request);
      if (problem) return json(res, 400, {error: problem});
      request.bookerCountry ||= "sa";
      request.currency ||= "SAR";
      const settled = await Promise.all(providers.map(p => p.search(request)));
      const names = [...new Set(settled.flatMap(r => r.offers.map(o => o.hotelName)))];
      const leads = await discovery.discover(request, names);
      return json(res, 200, buildSearchResponse(request, settled, leads));
    }
    return json(res, 404, {error: "not found"});
  } catch (error) {
    return json(res, 500, {error: error instanceof Error ? error.message : String(error)});
  }
});

const port = Number(process.env.PORT || 8787);
server.listen(port, () => console.log(`iqtans-engine listening on ${port}`));
