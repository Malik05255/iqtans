import { clampDiscount } from "./money.js";
import type { NormalizedOffer, PaymentCardProfile, PromotionRule, SearchRequest } from "./types.js";

const SOURCE_KINDS = new Set(["BANK", "HOTEL", "OTA", "CARD_NETWORK", "OFFICIAL_CAMPAIGN"]);

export function isTrustedPromotionRule(rule: PromotionRule): boolean {
  if (!rule?.id?.trim() || !rule.title?.trim()) return false;
  if (!rule.officialSource || !rule.sourceKind || !SOURCE_KINDS.has(rule.sourceKind)) return false;
  if (!/^https:\/\//i.test(rule.sourceUrl || "")) return false;
  if (!Number.isFinite(new Date(rule.verifiedAt).getTime())) return false;
  const hasDiscount = (rule.percentOff ?? 0) > 0 || (rule.flatOff ?? 0) > 0;
  if (!hasDiscount) return false;
  if ((rule.percentOff ?? 0) < 0 || (rule.percentOff ?? 0) > 100) return false;
  if ((rule.flatOff ?? 0) < 0 || (rule.maxDiscount ?? 0) < 0 || (rule.minSpend ?? 0) < 0) return false;
  if ((rule.minNights ?? 0) < 0 || (rule.maxNights ?? Number.MAX_SAFE_INTEGER) < (rule.minNights ?? 0)) return false;
  return true;
}

export function loadPromotionRules(): PromotionRule[] {
  const raw = process.env.PROMOTION_RULES_JSON;
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed.filter(isTrustedPromotionRule) : [];
  } catch {
    return [];
  }
}

function textMatch(allowed: string[] | undefined, actual: string | undefined): boolean {
  if (!allowed?.length) return true;
  if (!actual) return false;
  const a = actual.trim().toLowerCase();
  return allowed.some(v => v.trim().toLowerCase() === a);
}

function cardEligible(rule: PromotionRule, cards: PaymentCardProfile[]): PaymentCardProfile | undefined {
  if (!rule.banks?.length && !rule.networks?.length && !rule.tiers?.length) return {};
  return cards.find(card => textMatch(rule.banks, card.bank) && textMatch(rule.networks, card.network) && textMatch(rule.tiers, card.tier));
}

function validNow(rule: PromotionRule, now = new Date()): boolean {
  const t = now.getTime();
  if (rule.validFrom && t < new Date(rule.validFrom).getTime()) return false;
  if (rule.validUntil && t > new Date(rule.validUntil).getTime()) return false;
  return true;
}

function nights(request: SearchRequest): number {
  const start = new Date(`${request.checkIn}T00:00:00Z`).getTime();
  const end = new Date(`${request.checkOut}T00:00:00Z`).getTime();
  return Math.round((end - start) / 86_400_000);
}

function stayEligible(rule: PromotionRule, request: SearchRequest): boolean {
  const checkIn = new Date(`${request.checkIn}T00:00:00Z`).getTime();
  const checkOut = new Date(`${request.checkOut}T00:00:00Z`).getTime();
  if (rule.stayFrom && checkIn < new Date(`${rule.stayFrom}T00:00:00Z`).getTime()) return false;
  if (rule.stayUntil && checkOut > new Date(`${rule.stayUntil}T23:59:59Z`).getTime()) return false;
  const n = nights(request);
  if (rule.minNights != null && n < rule.minNights) return false;
  if (rule.maxNights != null && n > rule.maxNights) return false;
  if (!textMatch(rule.bookerCountries, request.bookerCountry)) return false;
  if (!textMatch(rule.currencies, request.currency)) return false;
  return true;
}

export function applyVerifiedPromotions(
  offer: NormalizedOffer,
  rules: PromotionRule[],
  cards: PaymentCardProfile[] = [],
  request?: SearchRequest
): NormalizedOffer[] {
  const variants: NormalizedOffer[] = [];
  for (const rule of rules) {
    if (!isTrustedPromotionRule(rule) || !validNow(rule)) continue;
    if (request && !stayEligible(rule, request)) continue;
    if (rule.providers?.length && !rule.providers.some(p => p.toLowerCase() === offer.provider.toLowerCase())) continue;
    if ((rule.minSpend ?? 0) > offer.totalPrice) continue;
    const card = cardEligible(rule, cards);
    if (!card) continue;

    let discount = rule.flatOff ?? 0;
    if (rule.percentOff) discount += offer.totalPrice * rule.percentOff / 100;
    if (rule.maxDiscount != null) discount = Math.min(discount, rule.maxDiscount);
    discount = clampDiscount(offer.totalPrice, discount);
    if (discount <= 0) continue;

    const reqs = [...offer.requirements, `عرض موثق من مصدر رسمي: ${rule.sourceKind}`];
    if (rule.requiresMembership) reqs.push("إنشاء عضوية/حساب وفق شروط العرض");
    if (rule.requiresApp) reqs.push("إتمام الحجز من التطبيق وفق شروط العرض");
    if (rule.code) reqs.push(`استخدام الكود ${rule.code}`);
    if (card.bank || card.network || card.tier) reqs.push(`الدفع ببطاقة مطابقة: ${[card.bank, card.network, card.tier].filter(Boolean).join(" ")}`);
    if (rule.stayFrom || rule.stayUntil) reqs.push(`فترة الإقامة المؤهلة: ${rule.stayFrom ?? "مفتوح"} — ${rule.stayUntil ?? "مفتوح"}`);

    variants.push({
      ...offer,
      id: `${offer.id}:promo:${rule.id}`,
      totalPrice: Math.round((offer.totalPrice - discount) * 100) / 100,
      strategyKind: card.bank || card.network || card.tier ? "CARD" : "PROMO",
      method: `${offer.provider} + ${rule.title}`,
      evidenceUrl: rule.sourceUrl,
      verifiedAt: rule.verifiedAt,
      requirements: reqs,
      breakdown: {...offer.breakdown, room: Math.max(0, Math.round((offer.breakdown.room - discount) * 100) / 100)}
    });
  }
  return variants;
}
