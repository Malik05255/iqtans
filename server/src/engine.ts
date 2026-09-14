import { applyVerifiedPromotions, loadPromotionRules } from "./promotions.js";
import type { DiscoveryLead, NormalizedOffer, ProviderResult, RankedHotel, SearchRequest, SearchResponse } from "./types.js";

function hotelKey(name: string): string {
  return name.toLowerCase().normalize("NFKD").replace(/[^\p{L}\p{N}]+/gu, " ").trim();
}

function relatedLead(lead: DiscoveryLead, hotelName: string): boolean {
  const h = hotelKey(hotelName);
  const text = hotelKey(`${lead.title} ${lead.description ?? ""}`);
  const tokens = h.split(" ").filter(t => t.length > 2);
  return tokens.length > 0 && tokens.filter(t => text.includes(t)).length >= Math.min(2, tokens.length);
}

export function buildSearchResponse(request: SearchRequest, providerResults: ProviderResult[], discoveries: DiscoveryLead[]): SearchResponse {
  const rules = loadPromotionRules();
  const offers = providerResults.flatMap(r => r.offers).flatMap(o => [o, ...applyVerifiedPromotions(o, rules, request.cards)]);
  const groups = new Map<string, NormalizedOffer[]>();
  for (const offer of offers) {
    const key = hotelKey(offer.hotelName);
    const list = groups.get(key) ?? [];
    list.push(offer);
    groups.set(key, list);
  }

  const hotels: RankedHotel[] = [...groups.entries()].map(([key, list]) => {
    const sorted = [...list].sort((a, b) => a.totalPrice - b.totalPrice);
    const standard = sorted.filter(o => o.strategyKind === "STANDARD" && o.verification === "LIVE_VERIFIED");
    const baseline = (standard.length ? standard : sorted).reduce((m, o) => Math.min(m, o.totalPrice), Number.POSITIVE_INFINITY);
    const best = sorted[0]?.totalPrice ?? baseline;
    const savings = Math.max(0, baseline - best);
    return {
      key,
      name: sorted[0].hotelName,
      city: sorted[0].city,
      baselinePrice: baseline,
      bestPrice: best,
      savings,
      savingsPercent: baseline > 0 ? Math.round(savings * 100 / baseline) : 0,
      offers: sorted,
      discoveries: discoveries.filter(d => relatedLead(d, sorted[0].hotelName)).slice(0, 12)
    };
  }).sort((a, b) => a.bestPrice - b.bestPrice);

  const configured = providerResults.filter(r => r.configured);
  const successful = configured.filter(r => r.offers.length > 0 && !r.warning);
  const mode: SearchResponse["mode"] = configured.length === 0 ? "unconfigured" : successful.length === configured.length ? "live" : "partial";

  return {
    request,
    generatedAt: new Date().toISOString(),
    mode,
    providers: providerResults.map(r => ({name: r.provider, configured: r.configured, warning: r.warning, latencyMs: r.latencyMs})),
    hotels,
    disclaimer: "السعر المؤكد مرتبط بوقت آخر تحقق وبنفس التواريخ والنزلاء والغرفة والشروط. نتائج اكتشاف الويب لا تُحتسب كتوفير حتى تُثبت شروطها ويُعاد فحص السعر."
  };
}
