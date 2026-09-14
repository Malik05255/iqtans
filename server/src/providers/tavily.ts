import type { DiscoveryLead, DiscoveryProvider, SearchRequest } from "../types.js";

export class TavilyOfferDiscovery implements DiscoveryProvider {
  readonly name = "Tavily Web Discovery";
  isConfigured(): boolean { return Boolean(process.env.TAVILY_API_KEY); }

  private async query(q: string): Promise<DiscoveryLead[]> {
    const response = await fetch("https://api.tavily.com/search", {
      method: "POST",
      headers: {
        "Authorization": `Bearer ${process.env.TAVILY_API_KEY || ""}`,
        "Content-Type": "application/json"
      },
      body: JSON.stringify({
        query: q,
        search_depth: "basic",
        topic: "general",
        country: "saudi arabia",
        max_results: 8,
        include_answer: false,
        include_raw_content: false
      })
    });
    if (!response.ok) return [];
    const json: any = await response.json();
    const now = new Date().toISOString();
    return (Array.isArray(json?.results) ? json.results : []).flatMap((r: any) => {
      const url = String(r?.url || "");
      if (!url.startsWith("http")) return [];
      let source = "tavily";
      try { source = new URL(url).hostname; } catch { /* ignore malformed source */ }
      return [{
        title: String(r?.title || "عرض مكتشف"),
        url,
        description: r?.content ? String(r.content) : undefined,
        source,
        query: q,
        discoveredAt: now,
        verification: "DISCOVERED" as const
      }];
    });
  }

  async discover(request: SearchRequest, hotelNames: string[]): Promise<DiscoveryLead[]> {
    if (!this.isConfigured()) return [];
    const mainHotel = hotelNames[0] || request.hotelName || request.city;
    const names = hotelNames.slice(0, 3).join(" OR ") || mainHotel;
    const queries = [
      `"${mainHotel}" ${request.city} hotel official member rate promo discount offer`,
      `"${mainHotel}" ${request.city} خصم عرض كوبون حجز فندق عضوية`,
      `(${names}) (Visa OR Mastercard OR mada OR الراجحي OR الأهلي OR الرياض OR الإنماء) hotel offer`,
      `"${mainHotel}" (X OR Twitter OR Instagram OR TikTok) hotel promo discount عرض خصم`
    ];
    const batches = await Promise.allSettled(queries.map(q => this.query(q)));
    const seen = new Set<string>();
    return batches
      .flatMap(r => r.status === "fulfilled" ? r.value : [])
      .filter(x => !seen.has(x.url) && seen.add(x.url))
      .slice(0, 40);
  }
}
