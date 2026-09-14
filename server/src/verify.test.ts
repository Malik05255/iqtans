import test from "node:test";
import assert from "node:assert/strict";
import { verificationResponse } from "./verify.js";
import type { NormalizedOffer, SearchResponse, VerifyRequest } from "./types.js";

const offer: NormalizedOffer = {
  id:"b", provider:"Booking.com", providerHotelId:"42", hotelName:"Hotel", city:"Jeddah", roomName:"King Room",
  checkIn:"2026-10-10",checkOut:"2026-10-12",adults:2,rooms:1,totalPrice:390,
  breakdown:{room:350,taxes:0,mandatoryFees:40,payAtProperty:0,currency:"SAR"},strategyKind:"STANDARD",method:"live",
  verification:"LIVE_VERIFIED",verifiedAt:"now",matchPercent:100,requirements:[]
};
const response: SearchResponse = {request:{city:"Jeddah",checkIn:"2026-10-10",checkOut:"2026-10-12",adults:2,rooms:1},generatedAt:"now",mode:"live",providers:[],disclaimer:"",hotels:[{key:"hotel",name:"Hotel",city:"Jeddah",baselinePrice:390,bestPrice:390,savings:0,savingsPercent:0,offers:[offer],discoveries:[]}]};
const request: VerifyRequest = {search:response.request,provider:"Booking.com",providerHotelId:"42",expectedPrice:400,roomName:"King Room"};

test("reverification returns exact room and detects price change", () => {
  const result = verificationResponse(request,response);
  assert.equal(result.available,true);
  assert.equal(result.currentPrice,390);
  assert.equal(result.changed,true);
});

test("wrong room is not treated as exact", () => {
  const result = verificationResponse({...request,roomName:"Twin Room"},response);
  assert.equal(result.available,false);
});
