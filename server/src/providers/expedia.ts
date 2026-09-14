import { createHash } from "node:crypto";
import { money } from "../money.js";
import type { NormalizedOffer, ProviderResult, SearchProvider, SearchRequest } from "../types.js";

export class ExpediaRapidProvider implements SearchProvider {
  readonly name = "Expedia Rapid";
  isConfigured(): boolean { return Boolean(process.env.EXPEDIA_API_KEY && process.env.EXPEDIA_SHARED_SECRET); }
  private auth(): string {
    const key = process.env.EXPEDIA_API_KEY || "";
    const secret = process.env.EXPEDIA_SHARED_SECRET || "";
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const signature = createHash("sha512").update(key + secret + timestamp).digest("hex");
    return `EAN APIKey=${key},Signature=${signature},timestamp=${timestamp}`;
  }
  private headers() { return {Authorization: this.auth(), Accept: "application/json", "Accept-Encoding": "gzip"}; }
  private base(): string { return process.env.EXPEDIA_BASE_URL || "https://api.ean.com"; }

  private async getJson(path: string, params: Record<string, string | number | undefined>): Promise<any> {
    const url = new URL(path, this.base());
    for (const [k, v] of Object.entries(params)) if (v !== undefined) url.searchParams.append(k, String(v));
    const res = await fetch(url, {headers: this.headers()});
    if (!res.ok) throw new Error(`Expedia ${url.pathname} HTTP ${res.status}: ${await res.text()}`);
    return res.json();
  }

  private async propertyIds(city: string): Promise<string[]> {
    const suggestions = await this.getJson(process.env.EXPEDIA_TYPEAHEAD_PATH || "/v3/typeahead", {language: "ar-SA", text: city, type: "city", line_of_business: "properties", limit: 5});
    const list: any[] = suggestions?.results || suggestions?.data || suggestions?.suggestions || [];
    const regionId = list.map(x => x.region_id || x.region?.id || x.id).find(Boolean);
    if (!regionId) throw new Error(`Expedia region not found for ${city}`);
    const regions = await this.getJson(process.env.EXPEDIA_GEOGRAPHY_PATH || "/v2/regions", {language: "ar-SA", region_id: regionId, include: "property_ids"});
    const region = regions?.regions?.[0] || regions?.data?.[0] || regions;
    const ids = region?.property_ids || region?.propertyIds || [];
    return Array.isArray(ids) ? ids.map(String).slice(0, 250) : [];
  }

  async search(request: SearchRequest): Promise<ProviderResult> {
    const started = Date.now();
    if (!this.isConfigured()) return {provider: this.name, configured: false, offers: [], warning: "EXPEDIA_API_KEY / EXPEDIA_SHARED_SECRET غير مضبوطة", latencyMs: Date.now() - started};
    try {
      const ids = await this.propertyIds(request.city);
      if (!ids.length) throw new Error("Expedia returned no property ids for selected city");
      const occupancy = request.childrenAges?.length ? `${request.adults}-${request.childrenAges.join(",")}` : String(request.adults);
      const payload = await this.getJson(process.env.EXPEDIA_SHOPPING_PATH || "/v3/properties/availability", {
        property_id: ids.join(","), checkin: request.checkIn, checkout: request.checkOut,
        currency: request.currency || "SAR", language: "ar-SA", country_code: (request.bookerCountry || "sa").toUpperCase(), occupancy
      });
      const properties: any[] = Array.isArray(payload) ? payload : (payload?.data || payload?.properties || []);
      const now = new Date().toISOString();
      const offers: NormalizedOffer[] = [];
      for (const p of properties) {
        const hotelName = p.name || p.property_name || `Expedia #${p.property_id || p.id}`;
        if (request.hotelName && !String(hotelName).toLowerCase().includes(request.hotelName.toLowerCase())) continue;
        const rooms: any[] = p.rooms || [];
        for (const room of rooms) {
          for (const rate of (room.rates || [])) {
            const total = money(rate?.occupancy_pricing?.[occupancy]?.totals?.inclusive?.request_currency) || money(rate?.occupancy_pricing?.[occupancy]?.totals?.inclusive) || money(rate?.totals?.inclusive) || money(rate?.price);
            if (!(total > 0)) continue;
            offers.push({
              id: `expedia:${p.property_id || p.id}:${rate.id || offers.length}`,
              provider: "Expedia Rapid", providerHotelId: String(p.property_id || p.id), hotelName: String(hotelName), city: request.city,
              roomName: room.room_name || room.name, meal: rate?.amenities?.meal_plan || "حسب السعر", cancellation: rate?.refundable ? "قابل للإلغاء حسب الشروط" : "غير مسترد/راجع الشروط",
              checkIn: request.checkIn, checkOut: request.checkOut, adults: request.adults, rooms: request.rooms, totalPrice: total,
              breakdown: {room: total, taxes: 0, mandatoryFees: 0, payAtProperty: 0, currency: request.currency || "SAR"},
              strategyKind: "STANDARD", method: "السعر الحي من Expedia Rapid", bookingUrl: rate?.bed_groups?.links?.book?.href || rate?.links?.book?.href,
              evidenceUrl: "https://developers.expediagroup.com/rapid/lodging/shopping/about-shopping-api", verification: "LIVE_VERIFIED", verifiedAt: now,
              matchPercent: 100, requirements: []
            });
          }
        }
      }
      return {provider: this.name, configured: true, offers, latencyMs: Date.now() - started};
    } catch (error) {
      return {provider: this.name, configured: true, offers: [], warning: error instanceof Error ? error.message : String(error), latencyMs: Date.now() - started};
    }
  }
}
