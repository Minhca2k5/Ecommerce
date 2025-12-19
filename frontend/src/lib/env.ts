export function getApiBaseUrl() {
  const baseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;
  if (!baseUrl) throw new Error("Missing VITE_API_BASE_URL in frontend/.env");
  return baseUrl.replace(/\/+$/, "");
}

export function getAppBaseUrl() {
  const baseUrl = import.meta.env.VITE_APP_BASE_URL as string | undefined;
  if (baseUrl?.trim()) return baseUrl.replace(/\/+$/, "");
  return window.location.origin;
}

export function isAbsoluteUrl(url: string) {
  return url.startsWith("http://") || url.startsWith("https://");
}
