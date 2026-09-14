import test from "node:test";
import assert from "node:assert/strict";
import { hotelIdentityKey } from "./hotelIdentity.js";

test("reordered English hotel names with generic hotel word resolve to one key", () => {
  assert.equal(hotelIdentityKey("Jeddah Hilton Hotel"), hotelIdentityKey("Hilton Jeddah"));
});

test("Arabic generic hotel prefix does not duplicate the same property", () => {
  assert.equal(hotelIdentityKey("فندق هيلتون جدة"), hotelIdentityKey("هيلتون جدة"));
});

test("distinct property token remains distinct", () => {
  assert.notEqual(hotelIdentityKey("Hilton Jeddah"), hotelIdentityKey("Hilton Makkah"));
});
