import type { RankedHotel, SearchRequest, SearchResponse } from "./types.js";

export function shiftedRequest(request: SearchRequest, days: number): SearchRequest {
  return {
    ...request,
    checkIn: shiftIsoDate(request.checkIn, days),
    checkOut: shiftIsoDate(request.checkOut, days),
    flexibilityDays: 0
  };
}

function shiftIsoDate(value: string, days: number): string {
  const date = new Date(`${value}T00:00:00Z`);
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

function providerIdentities(hotel: RankedHotel): Set<string> {
  return new Set(hotel.offers.map(o => `${o.provider.toLowerCase()}|${o.providerHotelId}`));
}

function sameHotel(a: RankedHotel, b: RankedHotel): boolean {
  const aIds = providerIdentities(a);
  if (b.offers.some(o => aIds.has(`${o.provider.toLowerCase()}|${o.providerHotelId}`))) return true;
  return a.key === b.key;
}

export function attachBestFlexibleDate(base: SearchResponse, alternatives: SearchResponse[]): SearchResponse {
  if (!alternatives.length) return base;
  return {
    ...base,
    hotels: base.hotels.map(hotel => {
      let best: RankedHotel | undefined;
      let bestRequest: SearchRequest | undefined;
      for (const alt of alternatives) {
        const match = alt.hotels.find(candidate => sameHotel(hotel, candidate));
        if (!match || match.bestPrice >= hotel.bestPrice) continue;
        if (!best || match.bestPrice < best.bestPrice) {
          best = match;
          bestRequest = alt.request;
        }
      }
      if (!best || !bestRequest) return hotel;
      const msPerDay = 86_400_000;
      const shift = Math.round((new Date(`${bestRequest.checkIn}T00:00:00Z`).getTime() - new Date(`${base.request.checkIn}T00:00:00Z`).getTime()) / msPerDay);
      return {
        ...hotel,
        flexible: {
          checkIn: bestRequest.checkIn,
          checkOut: bestRequest.checkOut,
          bestPrice: best.bestPrice,
          savings: Math.max(0, hotel.bestPrice - best.bestPrice),
          dayShift: shift
        }
      };
    })
  };
}
