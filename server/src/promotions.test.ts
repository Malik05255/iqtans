import test from "node:test";
import assert from "node:assert/strict";
import { applyVerifiedPromotions, isTrustedPromotionRule } from "./promotions.js";
import type { NormalizedOffer, PromotionRule, SearchRequest } from "./types.js";

const offer: NormalizedOffer = {
  id:"x", provider:"Booking.com", providerHotelId:"1", hotelName:"Hotel", city:"Jeddah", checkIn:"2026-10-01", checkOut:"2026-10-03", adults:2, rooms:1,
  totalPrice:400, breakdown:{room:400,taxes:0,mandatoryFees:0,payAtProperty:0,currency:"SAR"}, strategyKind:"STANDARD", method:"standard",
  verification:"LIVE_VERIFIED", verifiedAt:"2026-09-14T00:00:00Z", matchPercent:100, requirements:[]
};

const request: SearchRequest = {
  city:"Jeddah", checkIn:"2026-10-01", checkOut:"2026-10-03", adults:2, rooms:1, bookerCountry:"sa", currency:"SAR"
};

function trusted(overrides: Partial<PromotionRule> = {}): PromotionRule {
  return {
    id:"r", title:"20%", sourceUrl:"https://bank.example/offer", verifiedAt:"2026-09-14T00:00:00Z",
    officialSource:true, sourceKind:"BANK", percentOff:20, banks:["Bank A"], ...overrides
  };
}

test("card promotion respects min spend and max discount", () => {
  const result = applyVerifiedPromotions(offer, [trusted({maxDiscount:50,minSpend:300})], [{bank:"Bank A"}], request);
  assert.equal(result[0].totalPrice, 350);
});

test("ineligible card is not applied", () => {
  assert.equal(applyVerifiedPromotions(offer, [trusted()], [{bank:"Bank B"}], request).length, 0);
});

test("unofficial promotion is never trusted", () => {
  const rule = trusted({officialSource:false});
  assert.equal(isTrustedPromotionRule(rule), false);
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], request).length, 0);
});

test("stay window, currency, country and nights must match", () => {
  const rule = trusted({
    stayFrom:"2026-10-01", stayUntil:"2026-10-05", currencies:["SAR"], bookerCountries:["sa"], minNights:2, maxNights:4
  });
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], request).length, 1);
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], {...request, checkIn:"2026-10-06", checkOut:"2026-10-08"}).length, 0);
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], {...request, currency:"USD"}).length, 0);
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], {...request, bookerCountry:"ae"}).length, 0);
  assert.equal(applyVerifiedPromotions(offer, [rule], [{bank:"Bank A"}], {...request, checkOut:"2026-10-02"}).length, 0);
});
