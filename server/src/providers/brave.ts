import type { DiscoveryLead, DiscoveryProvider, SearchRequest } from "../types.js";

export class BraveOfferDiscovery implements DiscoveryProvider {
  readonly name = "Brave Web Discovery";
  isConfigured(): boolean { return Boolean(process.env.BRAVE_SEARCH_API_KEY); }

  private async query(q: string): Promise<DiscoveryLead[]> {
    const url = new URL("https://api.search.brave.com/res/v1/web/search");
    url.searchParams.set("q", q);
    url.searchParams.set("country", "SA");
    url.searchParams.set("search_lang", "ar");
    url.searchParams.set("count", "20");
    const res = await fetch(url, {headers: {"X-Subscription-Token": process.env.BRAVE_SEARCH_API_KEY || "", Accept: "application/json"}});
    if (!res.ok) return [];
    const json: any = await res.json();
    return (json?.web?.results || []).map((r: any) => ({
      title: String(r.title || "عرض مكتشف"), url: String(r.url), description: r.description ? String(r.description) : undefined,
      source: new URL(String(r.url)).hostname, query: q, discoveredAt: new Date().toISOString(), verification: "DISCOVERED" as const
    }));
  }

  async discover(request: SearchRequest, hotelNames: string[]): Promise<DiscoveryLead[]> {
    if (!this.isConfigured()) return [];
    const targets = hotelNames.length ? hotelNames.slice(0, 8) : [request.hotelName || request.city];
    const queries = targets.flatMap(name => [
      `"${name}" ${request.city} خصم عرض كوبون فندق حجز بطاقة بنك`,
      `"${name}" hotel promo member rate app discount ${request.checkIn} ${request.checkOut}`,
      `"${name}" (site:x.com OR site:instagram.com OR site:tiktok.com) عرض OR خصم OR promo`,
      `"${name}" (Visa OR Mastercard OR الراجحي OR الأهلي OR الرياض OR الإنماء) hotel offer`
    ]).slice(0, 20);
    const batches = await Promise.allSettled(queries.map(q => this.query(q)));
    const seen = new Set<string>();
    return batches.flatMap(r => r.status === "fulfilled" ? r.value : []).filter(x => !seen.has(x.url) && seen.add(x.url)).slice(0, 100);
  }
}
