export type Verification = "LIVE_VERIFIED" | "DISCOVERED" | "UNVERIFIED";
export type StrategyKind = "STANDARD" | "MEMBER" | "APP" | "CARD" | "PROMO" | "PACKAGE";
export type PromotionSourceKind = "BANK" | "HOTEL" | "OTA" | "CARD_NETWORK" | "OFFICIAL_CAMPAIGN";

export interface PaymentCardProfile { bank?: string; network?: string; tier?: string; country?: string; }
export interface SearchRequest {
  city: string; hotelName?: string; checkIn: string; checkOut: string; adults: number; childrenAges?: number[]; rooms: number;
  bookerCountry?: string; currency?: string; flexibilityDays?: number; cards?: PaymentCardProfile[];
}
export interface PriceBreakdown { room: number; taxes: number; mandatoryFees: number; payAtProperty: number; currency: string; }
export interface NormalizedOffer {
  id: string; provider: string; providerHotelId: string; hotelName: string; city: string; stars?: number; rating?: number; ratingCount?: number;
  roomName?: string; meal?: string; cancellation?: string; checkIn: string; checkOut: string; adults: number; rooms: number; totalPrice: number;
  breakdown: PriceBreakdown; strategyKind: StrategyKind; method: string; bookingUrl?: string; evidenceUrl?: string; verification: Verification;
  verifiedAt: string; matchPercent: number; requirements: string[];
  comparisonPrice?: number; comparisonReason?: string;
}
export interface PromotionRule {
  id: string; title: string; sourceUrl: string; verifiedAt: string;
  officialSource?: boolean; sourceKind?: PromotionSourceKind;
  validFrom?: string; validUntil?: string; stayFrom?: string; stayUntil?: string;
  providers?: string[]; banks?: string[]; networks?: string[]; tiers?: string[];
  bookerCountries?: string[]; currencies?: string[]; minNights?: number; maxNights?: number;
  minSpend?: number; percentOff?: number; flatOff?: number; maxDiscount?: number; code?: string;
  requiresApp?: boolean; requiresMembership?: boolean; stackable?: boolean;
}
export interface DiscoveryLead { title: string; url: string; description?: string; source: string; query: string; discoveredAt: string; verification: "DISCOVERED"; }
export interface ProviderResult { provider: string; configured: boolean; offers: NormalizedOffer[]; warning?: string; latencyMs: number; }
export interface SearchProvider { readonly name: string; isConfigured(): boolean; search(request: SearchRequest): Promise<ProviderResult>; }
export interface DiscoveryProvider { readonly name: string; isConfigured(): boolean; discover(request: SearchRequest, hotelNames: string[]): Promise<DiscoveryLead[]>; }
export interface FlexibleDateOption { checkIn: string; checkOut: string; bestPrice: number; savings: number; dayShift: number; }
export interface RankedHotel {
  key: string; name: string; city: string; baselinePrice: number; bestPrice: number; savings: number; savingsPercent: number;
  offers: NormalizedOffer[]; discoveries: DiscoveryLead[]; flexible?: FlexibleDateOption;
}
export interface SearchCoverage {
  priceProvidersTotal: number;
  priceProvidersConfigured: number;
  priceProvidersWithResults: number;
  liveOffers: number;
  discoveredLeads: number;
}
export interface SearchResponse {
  request: SearchRequest; generatedAt: string; mode: "live" | "partial" | "unconfigured";
  providers: Array<{name: string; configured: boolean; warning?: string; latencyMs: number}>;
  coverage?: SearchCoverage;
  hotels: RankedHotel[]; disclaimer: string;
}
export interface VerifyRequest {
  search: SearchRequest;
  provider: string;
  providerHotelId: string;
  expectedPrice: number;
  roomName?: string;
}
export interface VerifyResponse {
  available: boolean;
  expectedPrice: number;
  currentPrice?: number;
  changed: boolean;
  verifiedAt: string;
  offer?: NormalizedOffer;
  reason?: string;
}
