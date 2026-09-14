import type { ReviewProvider, ReviewProviderResult, ReviewSummary, ExternalReviewRecord } from "../types.js";

export function normalizeGooglePlaceReview(hotelName: string, place: any): ExternalReviewRecord | null {
  const score = Number(place?.rating || 0);
  if (!(score > 0)) return null;
  const count = Math.max(0, Math.trunc(Number(place?.userRatingCount || 0)));
  const sourceUrl = typeof place?.googleMapsUri === "string" && place.googleMapsUri ? place.googleMapsUri : undefined;
  const review: ReviewSummary = {
    source: "Google Maps",
    score,
    scale: 5,
    count,
    attribution: "Google Maps",
    sourceUrl
  };
  return {hotelName, review};
}

export class GooglePlacesReviewProvider implements ReviewProvider {
  readonly name = "Google Maps Places";

  isConfigured(): boolean {
    return Boolean(process.env.GOOGLE_PLACES_API_KEY);
  }

  private async lookup(hotelName: string, city: string): Promise<ExternalReviewRecord | null> {
    const response = await fetch("https://places.googleapis.com/v1/places:searchText", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Goog-Api-Key": process.env.GOOGLE_PLACES_API_KEY || "",
        "X-Goog-FieldMask": "places.displayName,places.rating,places.userRatingCount,places.googleMapsUri"
      },
      body: JSON.stringify({
        textQuery: `${hotelName} ${city}`,
        languageCode: "ar",
        regionCode: "SA",
        pageSize: 1
      })
    });
    if (!response.ok) throw new Error(`Google Places HTTP ${response.status}`);
    const payload: any = await response.json();
    const place = Array.isArray(payload?.places) ? payload.places[0] : undefined;
    return place ? normalizeGooglePlaceReview(hotelName, place) : null;
  }

  async fetch(hotelNames: string[], city: string): Promise<ReviewProviderResult> {
    const started = Date.now();
    if (!this.isConfigured()) return {provider: this.name, configured: false, reviews: [], latencyMs: Date.now() - started};
    const configuredLimit = Number(process.env.GOOGLE_PLACES_REVIEW_LIMIT || 12);
    const limit = Math.min(50, Math.max(1, Number.isFinite(configuredLimit) ? Math.trunc(configuredLimit) : 12));
    const names = [...new Set(hotelNames.map(v => v.trim()).filter(Boolean))].slice(0, limit);
    const reviews: ExternalReviewRecord[] = [];
    let failures = 0;
    for (let i = 0; i < names.length; i += 4) {
      const settled = await Promise.allSettled(names.slice(i, i + 4).map(name => this.lookup(name, city)));
      for (const result of settled) {
        if (result.status === "fulfilled") {
          if (result.value) reviews.push(result.value);
        } else failures++;
      }
    }
    return {
      provider: this.name,
      configured: true,
      reviews,
      warning: failures > 0 ? `${failures} Google Places lookups failed` : undefined,
      latencyMs: Date.now() - started
    };
  }
}
