import test from "node:test";
import assert from "node:assert/strict";
import { normalizeGooglePlaceReview } from "./googlePlacesReviews.js";

test("normalizes Google Maps rating with attribution and source link", () => {
  const item = normalizeGooglePlaceReview("Hotel A", {
    rating: 4.6,
    userRatingCount: 1280,
    googleMapsUri: "https://maps.google.com/?cid=1"
  });
  assert.ok(item);
  assert.equal(item?.hotelName, "Hotel A");
  assert.equal(item?.review.source, "Google Maps");
  assert.equal(item?.review.scale, 5);
  assert.equal(item?.review.score, 4.6);
  assert.equal(item?.review.count, 1280);
  assert.equal(item?.review.attribution, "Google Maps");
  assert.equal(item?.review.sourceUrl, "https://maps.google.com/?cid=1");
});

test("ignores places without a rating", () => {
  assert.equal(normalizeGooglePlaceReview("Hotel B", {userRatingCount: 20}), null);
});
