import test from "node:test";
import assert from "node:assert/strict";
import { collectReviewSummaries } from "./engine.js";
import type { NormalizedOffer } from "./types.js";

function offer(provider: string, score: number, count: number, id: string): NormalizedOffer {
  return {
    id, provider, providerHotelId: id, hotelName: "Hotel", city: "Jeddah",
    rating: score, ratingCount: count,
    checkIn: "2026-10-01", checkOut: "2026-10-02", adults: 2, rooms: 1,
    totalPrice: 400, breakdown: {room: 400, taxes: 0, mandatoryFees: 0, payAtProperty: 0, currency: "SAR"},
    strategyKind: "STANDARD", method: "standard", verification: "LIVE_VERIFIED", verifiedAt: "2026-09-14T00:00:00Z",
    matchPercent: 100, requirements: []
  };
}

test("review summaries stay independent from cheapest offer and preserve source scales", () => {
  const reviews = collectReviewSummaries([
    offer("Booking.com", 9.1, 4800, "a"),
    offer("Booking.com", 9.1, 4800, "b"),
    offer("Google", 4.5, 7200, "c")
  ]);
  assert.equal(reviews.length, 2);
  assert.deepEqual(reviews[0], {source: "Google", score: 4.5, scale: 5, count: 7200});
  assert.deepEqual(reviews[1], {source: "Booking.com", score: 9.1, scale: 10, count: 4800});
});
