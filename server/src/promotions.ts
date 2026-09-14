import { clampDiscount } from "./money.js";
import type { NormalizedOffer, PaymentCardProfile, PromotionRule } from "./types.js";

export function loadPromotionRules(): PromotionRule[] {
  const raw = process.env.PROMOTION_RULES_JSON;
  if (!raw) return [];
  try {
    const parsed = JSON.parse(raw);
    return Array.isArray(parsed) ? parsed : [];
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

export function applyVerifiedPromotions(offer: NormalizedOffer, rules: PromotionRule[], cards: PaymentCardProfile[] = []): NormalizedOffer[] {
  const variants: NormalizedOffer[] = [];
  for (const rule of rules) {
    if (!validNow(rule)) continue;
    if (rule.providers?.length && !rule.providers.some(p => p.toLowerCase() === offer.provider.toLowerCase())) continue;
    if ((rule.minSpend ?? 0) > offer.totalPrice) continue;
    const card = cardEligible(rule, cards);
    if (!card) continue;

    let discount = rule.flatOff ?? 0;
    if (rule.percentOff) discount += offer.totalPrice * rule.percentOff / 100;
    if (rule.maxDiscount != null) discount = Math.min(discount, rule.maxDiscount);
    discount = clampDiscount(offer.totalPrice, discount);
    if (discount <= 0) continue;

    const reqs = [...offer.requirements];
    if (rule.requiresMembership) reqs.push("إنشاء عضوية/حساب وفق شروط العرض");
    if (rule.requiresApp) reqs.push("إتمام الحجز من التطبيق وفق شروط العرض");
    if (rule.code) reqs.push(`استخدام الكود ${rule.code}`);
    if (card.bank || card.network || card.tier) reqs.push(`الدفع ببطاقة مطابقة: ${[card.bank, card.network, card.tier].filter(Boolean).join(" ")}`);

    variants.push({
      ...offer,
      id: `${offer.id}:promo:${rule.id}`,
      totalPrice: Math.round((offer.totalPrice - discount) * 100) / 100,
      strategyKind: card.bank || card.network || card.tier ? "CARD" : "PROMO",
      method: `${offer.provider} + ${rule.title}`,
      evidenceUrl: rule.sourceUrl,
      verifiedAt: rule.verifiedAt,
      requirements: reqs,
      breakdown: {...offer.breakdown, room: Math.max(0, offer.breakdown.room - discount)}
    });
  }
  return variants;
}
