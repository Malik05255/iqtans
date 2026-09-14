import test from "node:test";
import assert from "node:assert/strict";
import { amadeusCityCoordinates, normalizeAmadeusOffer } from "./amadeus.js";
import type { SearchRequest } from "../types.js";

const request: SearchRequest = {city:"جدة",checkIn:"2026-10-01",checkOut:"2026-10-03",adults:2,rooms:2,currency:"SAR",bookerCountry:"sa"};

test("Amadeus Saudi city geocodes are configured", () => {
  assert.ok(amadeusCityCoordinates("جدة"));
  assert.ok(amadeusCityCoordinates("مكة المكرمة"));
  assert.ok(amadeusCityCoordinates("جازان"));
  assert.ok(amadeusCityCoordinates("أبها"));
  assert.ok(amadeusCityCoordinates("محايل عسير"));
});

test("Amadeus price is multiplied by requested room quantity", () => {
  const normalized = normalizeAmadeusOffer(
    {hotel:{hotelId:"AMJED001",name:"Hotel Test",rating:"5"}},
    {id:"offer-1",checkInDate:request.checkIn,checkOutDate:request.checkOut,price:{total:"425.50",currency:"SAR"},room:{description:{text:"King Room"}},boardType:"BREAKFAST"},
    request
  );
  assert.ok(normalized);
  assert.equal(normalized!.totalPrice,851);
  assert.equal(normalized!.rooms,2);
  assert.equal(normalized!.roomName,"King Room");
});
