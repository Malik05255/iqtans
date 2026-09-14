import test from "node:test";
import assert from "node:assert/strict";
import { applyVerifiedPromotions } from "./promotions.js";
import type { NormalizedOffer, PromotionRule } from "./types.js";

const offer: NormalizedOffer = {
  id:"x", provider:"Booking.com", providerHotelId:"1", hotelName:"Hotel", city:"Jeddah", checkIn:"2026-10-01", checkOut:"2026-10-02", adults:2, rooms:1,
  totalPrice:400, breakdown:{room:400,taxes:0,mandatoryFees:0,payAtProperty:0,currency:"SAR"}, strategyKind:"STANDARD", method:"standard",
  verification:"LIVE_VERIFIED", verifiedAt:"2026-09-14T00:00:00Z", matchPercent:100, requirements:[]
};

test("card promotion respects min spend and max discount", () => {
  const rule: PromotionRule = {id:"r", title:"20%", sourceUrl:"https://bank.example", verifiedAt:"2026-09-14", percentOff:20, maxDiscount:50, minSpend:300, banks:["Bank A"]};
  const result = applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}]);
  assert.equal(result[0].totalPrice, 350);
});

test("ineligible card is not applied", () => {
  const rule: PromotionRule = {id:"r", title:"20%", sourceUrl:"https://bank.example", verifiedAt:"2026-09-14", percentOff:20, banks:["Bank A"]};
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank B"}]).length, 0);
});
