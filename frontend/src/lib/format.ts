export function formatCurrency(value: number | null | undefined, currency?: string) {
  if (value === null || value === undefined || Number.isNaN(value)) return "—";
  const resolvedCurrency = currency?.trim() ? currency : "VND";
  try {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: resolvedCurrency,
      maximumFractionDigits: 0,
    }).format(value);
  } catch {
    return `${value}`;
  }
}

export function formatCompactNumber(value: number | null | undefined) {
  if (value === null || value === undefined || Number.isNaN(value)) return "0";
  try {
    return new Intl.NumberFormat("vi-VN", { notation: "compact" }).format(value);
  } catch {
    return `${value}`;
  }
}

