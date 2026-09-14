export function money(value: unknown): number {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  if (typeof value === "string") {
    const n = Number(value.replace(/,/g, ""));
    return Number.isFinite(n) ? n : 0;
  }
  if (value && typeof value === "object") {
    const o = value as Record<string, unknown>;
    for (const key of ["amount", "value", "total", "display", "book", "booker_currency", "accommodation_currency"]) {
      const n = money(o[key]);
      if (n > 0) return n;
    }
  }
  return 0;
}

export function clampDiscount(price: number, discount: number): number {
  return Math.max(0, Math.min(price, discount));
}
