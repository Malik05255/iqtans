import test from "node:test";
import assert from "node:assert/strict";
import { buildSearchResponse, collectReviewSummaries } from "./engine.js";
import type { NormalizedOffer, ProviderResult, SearchRequest } from "./types.js";

function offer(provider: string, score: number, count: number, id: string, price = 400): NormalizedOffer {
  return {
    id, provider, providerHotelId: id, hotelName: "Hotel", city: "Jeddah",
    rating: score, ratingCount: count,
    roomName: "King Room", meal: "Breakfast", cancellation: "Free cancellation",
    checkIn: "2026-10-01", checkOut: "2026-10-02", adults: 2, rooms: 1,
    totalPrice: price, breakdown: {room: price, taxes: 0, mandatoryFees: 0, payAtProperty: 0, currency: "SAR"},
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

test("external attributed ratings enrich the hotel without changing price or savings", () => {
  const request: SearchRequest = {
    city: "Jeddah", checkIn: "2026-10-01", checkOut: "2026-10-02", adults: 2, rooms: 1, currency: "SAR", bookerCountry: "sa"
  };
  const result: ProviderResult = {
    provider: "Booking", configured: true, latencyMs: 1,
    offers: [offer("Booking.com", 9.1, 4800, "booking:1", 400)]
  };
  const response = buildSearchResponse(request, [result], [], [{hotelName: "Hotel", review: {
    source: "Google Maps", score: 4.6, scale: 5, count: 7200,
    attribution: "Google Maps", sourceUrl: "https://maps.google.com/?cid=1"
  }}], 1);
  const hotel = response.hotels[0];
  assert.equal(hotel.bestPrice, 400);
  assert.equal(hotel.baselinePrice, 400);
  assert.equal(hotel.savings, 0);
  assert.equal(hotel.reviews?.length, 2);
  assert.deepEqual(hotel.reviews?.[0], {
    source: "Google Maps", score: 4.6, scale: 5, count: 7200,
    attribution: "Google Maps", sourceUrl: "https://maps.google.com/?cid=1"
  });
  assert.equal(response.coverage?.reviewProvidersConfigured, 1);
  assert.equal(response.coverage?.externalReviewSources, 1);
});
