import { money } from "../money.js";
import type { NormalizedOffer, ProviderResult, SearchProvider, SearchRequest } from "../types.js";

export class BookingDemandProvider implements SearchProvider {
  readonly name = "Booking.com Demand";
  private cityCache: unknown[] | null = null;
  private cityCacheAt = 0;

  isConfigured(): boolean {
    return Boolean(process.env.BOOKING_API_TOKEN && process.env.BOOKING_AFFILIATE_ID);
  }

  private get base(): string { return process.env.BOOKING_BASE_URL || "https://demandapi.booking.com/3.2"; }

  private async post(path: string, body: unknown): Promise<any> {
    const response = await fetch(`${this.base}${path}`, {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.BOOKING_API_TOKEN}`,
        "X-Affiliate-Id": process.env.BOOKING_AFFILIATE_ID || "",
        "Content-Type": "application/json"
      },
      body: JSON.stringify(body)
    });
    if (!response.ok) throw new Error(`Booking ${path} HTTP ${response.status}: ${await response.text()}`);
    return response.json();
  }

  private names(value: any): string[] {
    if (typeof value === "string") return [value];
    if (!value || typeof value !== "object") return [];
    return Object.values(value).flatMap(v => this.names(v));
  }

  private async resolveCityId(city: string): Promise<number> {
    if (!this.cityCache || Date.now() - this.cityCacheAt > 24 * 60 * 60 * 1000) {
      const payload = await this.post("/common/locations/cities", {});
      this.cityCache = Array.isArray(payload.data) ? payload.data : [];
      this.cityCacheAt = Date.now();
    }
    const aliases: Record<string, string[]> = {
      "جدة": ["jeddah", "جدّة"], "مكة المكرمة": ["makkah", "mecca", "مكة"], "مكه المكرمه": ["makkah", "mecca", "مكة"],
      "جازان": ["jazan", "jizan"], "أبها": ["abha", "ابها"], "ابها": ["abha"], "محايل عسير": ["muhayil", "mahayel", "محايل"]
    };
    const needles = [city, ...(aliases[city] ?? [])].map(v => v.toLowerCase());
    for (const item of this.cityCache as any[]) {
      const names = this.names(item.name).map(v => v.toLowerCase());
      if (needles.some(n => names.some(x => x === n || x.includes(n)))) return Number(item.id);
    }
    throw new Error(`Booking city id not found for ${city}`);
  }

  private bestAmount(data: any): number {
    return money(data?.price?.total?.booker_currency) || money(data?.price?.total) || money(data?.price?.display?.booker_currency) || money(data?.price?.display) || money(data?.price);
  }

  async search(request: SearchRequest): Promise<ProviderResult> {
    const started = Date.now();
    if (!this.isConfigured()) return {provider: this.name, configured: false, offers: [], warning: "BOOKING_API_TOKEN / BOOKING_AFFILIATE_ID غير مضبوطة", latencyMs: Date.now() - started};
    try {
      const cityId = await this.resolveCityId(request.city);
      const payload = await this.post("/accommodations/search", {
        city: cityId,
        booker: {country: request.bookerCountry || "sa", platform: "mobile"},
        checkin: request.checkIn,
        checkout: request.checkOut,
        guests: {number_of_rooms: request.rooms, number_of_adults: request.adults, ...(request.childrenAges?.length ? {children: request.childrenAges} : {})},
        currency: request.currency || "SAR",
        rows: 100
      });
      const rows: any[] = Array.isArray(payload.data) ? payload.data : [];
      const ids = rows.slice(0, 100).map(r => r.id).filter(Boolean);
      let detailsById = new Map<string, any>();
      if (ids.length) {
        try {
          const details = await this.post("/accommodations/details", {accommodations: ids, extras: ["photos", "rooms"]});
          for (const d of (Array.isArray(details.data) ? details.data : [])) detailsById.set(String(d.id), d);
        } catch { /* Search remains usable if content lookup is unavailable. */ }
      }

      const now = new Date().toISOString();
      const offers: NormalizedOffer[] = rows.flatMap((r, index) => {
        const total = this.bestAmount(r);
        if (!(total > 0)) return [];
        const d = detailsById.get(String(r.id)) ?? {};
        const hotelName = this.names(d.name)[0] || this.names(r.name)[0] || `Booking #${r.id}`;
        if (request.hotelName && !hotelName.toLowerCase().includes(request.hotelName.toLowerCase())) return [];
        const currency = r?.currency?.booker || r?.currency || request.currency || "SAR";
        const url = r?.url?.web || r?.url || d?.url?.web;
        return [{
          id: `booking:${r.id}:${index}`,
          provider: "Booking.com",
          providerHotelId: String(r.id),
          hotelName,
          city: request.city,
          stars: Number(d?.rating?.stars || d?.stars || 0) || undefined,
          rating: Number(d?.rating?.review_score || d?.review_score || 0) || undefined,
          ratingCount: Number(d?.rating?.number_of_reviews || d?.number_of_reviews || 0) || undefined,
          roomName: r?.products?.[0]?.room_name || undefined,
          meal: r?.products?.[0]?.meal_plan || "حسب المنتج",
          cancellation: r?.products?.[0]?.policies?.cancellation?.type || "راجع الشروط",
          checkIn: request.checkIn,
          checkOut: request.checkOut,
          adults: request.adults,
          rooms: request.rooms,
          totalPrice: total,
          breakdown: {room: total, taxes: 0, mandatoryFees: 0, payAtProperty: 0, currency: String(currency)},
          strategyKind: "STANDARD",
          method: "السعر الحي من Booking.com",
          bookingUrl: typeof url === "string" ? url : undefined,
          evidenceUrl: "https://developers.booking.com/demand/docs/accommodations/about-accommodation",
          verification: "LIVE_VERIFIED",
          verifiedAt: now,
          matchPercent: 100,
          requirements: []
        } satisfies NormalizedOffer];
      });
      return {provider: this.name, configured: true, offers, latencyMs: Date.now() - started};
    } catch (error) {
      return {provider: this.name, configured: true, offers: [], warning: error instanceof Error ? error.message : String(error), latencyMs: Date.now() - started};
    }
  }
}
