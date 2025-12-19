const svg = (title: string, accent: string) =>
  `data:image/svg+xml;utf8,${encodeURIComponent(`<?xml version="1.0" encoding="UTF-8"?>
<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="900" viewBox="0 0 1200 900">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="${accent}" stop-opacity="0.35"/>
      <stop offset="50%" stop-color="#ffffff" stop-opacity="0.05"/>
      <stop offset="100%" stop-color="#ffffff" stop-opacity="0.0"/>
    </linearGradient>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="#0b1220"/>
      <stop offset="100%" stop-color="#111827"/>
    </linearGradient>
  </defs>
  <rect width="1200" height="900" rx="48" fill="url(#bg)"/>
  <rect width="1200" height="900" rx="48" fill="url(#g)"/>
  <circle cx="980" cy="220" r="190" fill="${accent}" opacity="0.18"/>
  <circle cx="240" cy="680" r="260" fill="${accent}" opacity="0.10"/>
  <text x="72" y="130" font-family="ui-sans-serif, system-ui" font-weight="700" font-size="54" fill="#e5e7eb">${title}</text>
  <text x="72" y="190" font-family="ui-sans-serif, system-ui" font-size="24" fill="#94a3b8">Image unavailable</text>
</svg>`)}`
    .replace(/\s+/g, " ");

export const PLACEHOLDER_IMAGES = [
  svg("Ecommerce", "#7c3aed"),
  svg("Ecommerce", "#06b6d4"),
  svg("Ecommerce", "#22c55e"),
  svg("Ecommerce", "#f97316"),
];

export function pickPlaceholder(key: string | number) {
  const index =
    typeof key === "number"
      ? Math.abs(key) % PLACEHOLDER_IMAGES.length
      : Math.abs(
          Array.from(String(key)).reduce((acc, ch) => acc + ch.charCodeAt(0), 0)
        ) % PLACEHOLDER_IMAGES.length;
  return PLACEHOLDER_IMAGES[index];
}

