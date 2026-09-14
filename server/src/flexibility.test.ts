import test from "node:test";
import assert from "node:assert/strict";
import { attachBestFlexibleDate, shiftedRequest } from "./flexibility.js";
import type { NormalizedOffer, SearchResponse } from "./types.js";

function offer(price: number, checkIn: string, checkOut: string): NormalizedOffer {
  return {
    id:`o-${price}`, provider:"Booking.com", providerHotelId:"42", hotelName:"Same Hotel", city:"Jeddah",
    checkIn, checkOut, adults:2, rooms:1, totalPrice:price,
    breakdown:{room:price,taxes:0,mandatoryFees:0,payAtProperty:0,currency:"SAR"},
    strategyKind:"STANDARD", method:"live", verification:"LIVE_VERIFIED", verifiedAt:"now", matchPercent:100, requirements:[]
  };
}
function response(price: number, checkIn: string, checkOut: string): SearchResponse {
  return {
    request:{city:"Jeddah",checkIn,checkOut,adults:2,rooms:1}, generatedAt:"now", mode:"live", providers:[], disclaimer:"",
    hotels:[{key:"same hotel",name:"Same Hotel",city:"Jeddah",baselinePrice:price,bestPrice:price,savings:0,savingsPercent:0,offers:[offer(price,checkIn,checkOut)],discoveries:[]}]
  };
}

test("shifted request preserves stay length", () => {
  const shifted = shiftedRequest({city:"Jeddah",checkIn:"2026-10-10",checkOut:"2026-10-13",adults:2,rooms:1,flexibilityDays:3}, -1);
  assert.equal(shifted.checkIn, "2026-10-09");
  assert.equal(shifted.checkOut, "2026-10-12");
});

test("best cheaper nearby date is attached for same provider hotel id", () => {
  const base = response(600,"2026-10-10","2026-10-13");
  const alt = response(430,"2026-10-09","2026-10-12");
  const result = attachBestFlexibleDate(base,[alt]);
  assert.equal(result.hotels[0].flexible?.savings,170);
  assert.equal(result.hotels[0].flexible?.dayShift,-1);
});
