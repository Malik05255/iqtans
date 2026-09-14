const GENERIC = new Set([
  "hotel", "hotels", "فندق", "الفندق", "the", "a", "an"
]);

export function normalizeHotelText(value: string): string {
  return value
    .toLowerCase()
    .normalize("NFKD")
    .replace(/\p{M}+/gu, "")
    .replace(/[^\p{L}\p{N}]+/gu, " ")
    .trim();
}

export function hotelIdentityKey(name: string): string {
  const tokens = normalizeHotelText(name)
    .split(" ")
    .filter(Boolean)
    .filter(token => !GENERIC.has(token))
    .sort((a, b) => a.localeCompare(b));
  return tokens.join(" ");
}

export function hotelNameTokens(name: string): string[] {
  return hotelIdentityKey(name).split(" ").filter(token => token.length > 1);
}
