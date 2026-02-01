import { getStoredTokens } from "@/lib/authStorage";
import { getApiBaseUrl } from "@/lib/env";

export function createAuthedEventSource(path: string) {
  const tokens = getStoredTokens();
  const accessToken = tokens?.accessToken;
  const url = `${getApiBaseUrl()}${path.startsWith("/") ? "" : "/"}${path}${
    accessToken ? `?access_token=${encodeURIComponent(accessToken)}` : ""
  }`;
  return new EventSource(url);
}
