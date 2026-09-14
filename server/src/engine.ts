import { applyVerifiedPromotions, loadPromotionRules } from "./promotions.js";
import { hotelIdentityKey, hotelNameTokens, normalizeHotelText } from "./hotelIdentity.js";
import type { DiscoveryLead, NormalizedOffer, ProviderResult, RankedHotel, ReviewSummary, SearchRequest, SearchResponse } from "./types.js";

function relatedLead(lead: DiscoveryLead, hotelName: string): boolean {
  const tokens = hotelNameTokens(hotelName).filter(t => t.length > 2);
  const text = normalizeHotelText(`${lead.title} ${lead.description ?? ""}`);
  return tokens.length > 0 && tokens.filter(t => text.includes(t)).length >= Math.min(2, tokens.length);
}

function paymentMode(offer: NormalizedOffer): string {
  const atProperty = Math.max(0, Number(offer.breakdown.payAtProperty || 0));
  if (atProperty <= 0.009) return "ONLINE";
  if (atProperty >= offer.totalPrice - 0.009) return "PROPERTY";
  return "SPLIT";
}

function productKey(offer: NormalizedOffer): string {
  const room = normalizeHotelText(offer.roomName || "");
  if (!room) return `${offer.id.split(":promo:")[0]}|${paymentMode(offer)}`;
  return [
    offer.provider.toLowerCase(), offer.providerHotelId, room,
    normalizeHotelText(offer.meal || ""), normalizeHotelText(offer.cancellation || ""), paymentMode(offer),
    offer.checkIn, offer.checkOut, offer.adults, offer.rooms
  ].join("|");
}

function inferRatingScale(score: number): number {
  return score > 5 ? 10 : 5;
}

export function collectReviewSummaries(input: NormalizedOffer[]): ReviewSummary[] {
  const bySource = new Map<string, ReviewSummary>();
  for (const offer of input) {
    const score = Number(offer.rating || 0);
    if (!(score > 0)) continue;
    const candidate: ReviewSummary = {
      source: offer.provider,
      score,
      scale: inferRatingScale(score),
      count: Math.max(0, Math.trunc(Number(offer.ratingCount || 0)))
    };
    const previous = bySource.get(candidate.source);
    if (!previous || candidate.count > previous.count) bySource.set(candidate.source, candidate);
  }
  return [...bySource.values()].sort((a, b) => b.count - a.count || b.score / b.scale - a.score / a.scale);
}

export function withFairComparisonPrices(input: NormalizedOffer[]): NormalizedOffer[] {
  const groups = new Map<string, NormalizedOffer[]>();
  for (const offer of input) {
    const key = productKey(offer);
    const list = groups.get(key) ?? [];
    list.push(offer);
    groups.set(key, list);
  }
  return input.map(offer => {
    const peers = groups.get(productKey(offer)) ?? [offer];
    const standards = peers.filter(p => p.strategyKind === "STANDARD" && p.verification === "LIVE_VERIFIED");
    const baseline = standards.length ? Math.min(...standards.map(p => p.totalPrice)) : offer.totalPrice;
    return {
      ...offer,
      comparisonPrice: baseline,
      comparisonReason: standards.length
        ? "مقارنة مع أرخص سعر قياسي حي لنفس الغرفة والوجبة وسياسة الإلغاء وطريقة الدفع لدى نفس مصدر الحجز"
        : "لا يوجد خط أساس مطابق بالكامل؛ لا نحتسب توفيرًا لهذا العرض"
    };
  });
}

export function buildSearchResponse(request: SearchRequest, providerResults: ProviderResult[], discoveries: DiscoveryLead[]): SearchResponse {
  const rules = loadPromotionRules();
  const rawOffers = providerResults.flatMap(r => r.offers).flatMap(o => [o, ...applyVerifiedPromotions(o, rules, request.cards, request)]);
  const offers = withFairComparisonPrices(rawOffers);
  const groups = new Map<string, NormalizedOffer[]>();
  for (const offer of offers) {
    const key = hotelIdentityKey(offer.hotelName) || normalizeHotelText(offer.hotelName);
    const list = groups.get(key) ?? [];
    list.push(offer);
    groups.set(key, list);
  }

  const hotels: RankedHotel[] = [...groups.entries()].map(([key, list]) => {
    const sorted = [...list].sort((a, b) => a.totalPrice - b.totalPrice);
    const winner = sorted[0];
    const baseline = winner?.comparisonPrice ?? winner?.totalPrice ?? 0;
    const best = winner?.totalPrice ?? baseline;
    const savings = Math.max(0, baseline - best);
    return {
      key,
      name: winner.hotelName,
      city: winner.city,
      baselinePrice: baseline,
      bestPrice: best,
      savings,
      savingsPercent: baseline > 0 ? Math.round(savings * 100 / baseline) : 0,
      offers: sorted,
      reviews: collectReviewSummaries(sorted),
      discoveries: discoveries.filter(d => relatedLead(d, winner.hotelName)).slice(0, 12)
    };
  }).sort((a, b) => a.bestPrice - b.bestPrice);

  const configured = providerResults.filter(r => r.configured);
  const successful = configured.filter(r => r.offers.length > 0 && !r.warning);
  const liveOffers = providerResults.flatMap(r => r.offers).filter(o => o.verification === "LIVE_VERIFIED").length;
  const mode: SearchResponse["mode"] = configured.length === 0 ? "unconfigured" : successful.length === configured.length ? "live" : "partial";

  return {
    request,
    generatedAt: new Date().toISOString(),
    mode,
    providers: providerResults.map(r => ({name: r.provider, configured: r.configured, warning: r.warning, latencyMs: r.latencyMs})),
    coverage: {
      priceProvidersTotal: providerResults.length,
      priceProvidersConfigured: configured.length,
      priceProvidersWithResults: providerResults.filter(r => r.configured && r.offers.length > 0).length,
      liveOffers,
      discoveredLeads: discoveries.length
    },
    hotels,
    disclaimer: "السعر المؤكد مرتبط بوقت آخر تحقق وبنفس التواريخ والنزلاء والغرفة والشروط. رقم «وفّرت» لا يُحسب إلا مقابل خط أساس مطابق للغرفة والوجبة وسياسة الإلغاء وطريقة الدفع، والعروض الرسمية يجب أن تطابق شروط الإقامة والبطاقة والعملة وبلد المستخدم."
  };
}
