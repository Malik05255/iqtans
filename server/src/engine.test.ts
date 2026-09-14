import test from "node:test";
import assert from "node:assert/strict";
import { buildSearchResponse, withFairComparisonPrices } from "./engine.js";
import type { NormalizedOffer, ProviderResult, SearchRequest } from "./types.js";

const request: SearchRequest = {city:"Jeddah",checkIn:"2026-10-01",checkOut:"2026-10-03",adults:2,rooms:1,currency:"SAR",bookerCountry:"sa"};

function offer(id: string, roomName: string, price: number, strategy: NormalizedOffer["strategyKind"], payAtProperty = 0): NormalizedOffer {
  return {
    id, provider:"Booking.com", providerHotelId:"1", hotelName:"Hotel A", city:"Jeddah", roomName, meal:"Breakfast", cancellation:"Free cancellation",
    checkIn:request.checkIn, checkOut:request.checkOut, adults:2, rooms:1, totalPrice:price,
    breakdown:{room:price,taxes:0,mandatoryFees:0,payAtProperty,currency:"SAR"}, strategyKind:strategy, method:strategy,
    verification:"LIVE_VERIFIED", verifiedAt:"2026-09-14T00:00:00Z", matchPercent:100, requirements:[]
  };
}

test("comparison price stays inside the same booking product", () => {
  const items = withFairComparisonPrices([
    offer("room-a", "King Room", 400, "STANDARD"),
    offer("room-a:promo:r", "King Room", 350, "CARD"),
    offer("room-b", "Suite", 300, "STANDARD")
  ]);
  const kingPromo = items.find(x => x.id.includes("promo"))!;
  const suite = items.find(x => x.roomName === "Suite")!;
  assert.equal(kingPromo.comparisonPrice, 400);
  assert.equal(suite.comparisonPrice, 300);
});

test("payment timing must also match before savings are calculated", () => {
  const items = withFairComparisonPrices([
    offer("room-a", "King Room", 400, "STANDARD", 0),
    offer("room-a:promo:r", "King Room", 350, "CARD", 350)
  ]);
  const promo = items.find(x => x.strategyKind === "CARD")!;
  assert.equal(promo.comparisonPrice, 350);
  assert.match(promo.comparisonReason || "", /لا يوجد خط أساس/);
});

test("hotel savings are zero when the absolute cheapest result is a different standard room", () => {
  const result: ProviderResult = {provider:"Booking",configured:true,latencyMs:1,offers:[
    offer("room-a", "King Room", 400, "STANDARD"),
    offer("room-a:promo:r", "King Room", 350, "CARD"),
    offer("room-b", "Suite", 300, "STANDARD")
  ]};
  const hotel = buildSearchResponse(request,[result],[]).hotels[0];
  assert.equal(hotel.bestPrice,300);
  assert.equal(hotel.baselinePrice,300);
  assert.equal(hotel.savings,0);
});

test("hotel savings use the matching standard baseline when a verified variant wins", () => {
  const result: ProviderResult = {provider:"Booking",configured:true,latencyMs:1,offers:[
    offer("room-a", "King Room", 400, "STANDARD"),
    offer("room-a:promo:r", "King Room", 250, "CARD"),
    offer("room-b", "Suite", 300, "STANDARD")
  ]};
  const hotel = buildSearchResponse(request,[result],[]).hotels[0];
  assert.equal(hotel.bestPrice,250);
  assert.equal(hotel.baselinePrice,400);
  assert.equal(hotel.savings,150);
});
