import type { NormalizedOffer, SearchResponse, VerifyRequest, VerifyResponse } from "./types.js";

function norm(value: string | undefined): string {
  return (value || "").toLowerCase().normalize("NFKD").replace(/[^\p{L}\p{N}]+/gu, " ").trim();
}

export function pickExactOffer(response: SearchResponse, request: VerifyRequest): NormalizedOffer | undefined {
  const provider = norm(request.provider);
  const expectedRoom = norm(request.roomName);
  const candidates = response.hotels
    .flatMap(h => h.offers)
    .filter(o => o.providerHotelId === request.providerHotelId)
    .filter(o => !provider || norm(o.provider).includes(provider) || provider.includes(norm(o.provider)))
    .filter(o => o.verification === "LIVE_VERIFIED" && o.matchPercent === 100)
    .filter(o => !expectedRoom || norm(o.roomName) === expectedRoom)
    .sort((a, b) => a.totalPrice - b.totalPrice);
  return candidates[0];
}

export function verificationResponse(request: VerifyRequest, response: SearchResponse): VerifyResponse {
  const offer = pickExactOffer(response, request);
  const now = new Date().toISOString();
  if (!offer) return {
    available: false,
    expectedPrice: request.expectedPrice,
    changed: true,
    verifiedAt: now,
    reason: "لم يعد هناك عرض حي بتطابق 100% لنفس الفندق والغرفة في نتيجة إعادة التحقق"
  };
  return {
    available: true,
    expectedPrice: request.expectedPrice,
    currentPrice: offer.totalPrice,
    changed: Math.abs(offer.totalPrice - request.expectedPrice) >= 0.01,
    verifiedAt: offer.verifiedAt || now,
    offer
  };
}
