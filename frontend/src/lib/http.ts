import { getApiBaseUrl } from "@/lib/env";
import { ApiError } from "@/lib/apiError";
import { clearStoredTokens, getStoredTokens, setStoredTokens } from "@/lib/authStorage";

type ApiJsonOptions = {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  body?: unknown;
  headers?: HeadersInit;
  auth?: boolean;
  signal?: AbortSignal;
};

let refreshInFlight: Promise<void> | null = null;

async function parseJsonSafe(response: Response) {
  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) return undefined;
  try {
    return (await response.json()) as unknown;
  } catch {
    return undefined;
  }
}

function buildUrl(path: string) {
  return `${getApiBaseUrl()}${path.startsWith("/") ? "" : "/"}${path}`;
}

function createRequestId() {
  if (typeof crypto !== "undefined" && "randomUUID" in crypto) {
    return crypto.randomUUID();
  }
  return `req_${Date.now()}_${Math.random().toString(16).slice(2)}`;
}

async function performRefreshIfNeeded() {
  const tokens = getStoredTokens();
  if (!tokens?.refreshToken) {
    clearStoredTokens();
    return;
  }

  const url = buildUrl("/api/auth/refresh-token");
  const response = await fetch(url, {
    method: "POST",
    headers: { Accept: "application/json", "Content-Type": "application/json" },
    body: JSON.stringify({ refreshToken: tokens.refreshToken }),
  });

  if (!response.ok) {
    clearStoredTokens();
    return;
  }

  const payload = (await response.json()) as { accessToken?: string; refreshToken?: string; tokenType?: string };
  if (!payload.accessToken || !payload.refreshToken) {
    clearStoredTokens();
    return;
  }

  setStoredTokens({
    accessToken: payload.accessToken,
    refreshToken: payload.refreshToken,
    tokenType: payload.tokenType || tokens.tokenType || "Bearer",
  });
}

async function ensureFreshTokensOnce() {
  if (!refreshInFlight) {
    refreshInFlight = performRefreshIfNeeded().finally(() => {
      refreshInFlight = null;
    });
  }
  await refreshInFlight;
}

export async function apiJson<T>(path: string, options: ApiJsonOptions = {}): Promise<T> {
  const url = buildUrl(path);
  const method = options.method ?? "GET";

  const headers: HeadersInit = {
    Accept: "application/json",
    ...(options.body !== undefined ? { "Content-Type": "application/json" } : {}),
    ...(options.headers ?? {}),
  };
  if (!(headers as Record<string, string>)["X-Request-Id"]) {
    (headers as Record<string, string>)["X-Request-Id"] = createRequestId();
  }

  const tokens = options.auth ? getStoredTokens() : null;
  if (options.auth && tokens?.accessToken) {
    (headers as Record<string, string>).Authorization = `${tokens.tokenType || "Bearer"} ${tokens.accessToken}`;
  }

  const fetchInit: RequestInit = {
    method,
    headers,
    signal: options.signal,
    ...(options.body !== undefined ? { body: JSON.stringify(options.body) } : {}),
  };

  let response = await fetch(url, fetchInit);

  if (options.auth && (response.status === 401 || response.status === 403)) {
    await ensureFreshTokensOnce();

    const refreshed = getStoredTokens();
    if (refreshed?.accessToken) {
      const retryHeaders: HeadersInit = {
        ...headers,
        Authorization: `${refreshed.tokenType || "Bearer"} ${refreshed.accessToken}`,
      };
      response = await fetch(url, { ...fetchInit, headers: retryHeaders });
    }
  }

  if (!response.ok) {
    const payload = await parseJsonSafe(response);
    throw new ApiError(`Request failed: ${response.status} ${response.statusText}`, response.status, payload);
  }

  if (response.status === 204) return undefined as T;

  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) return undefined as T;

  const text = await response.text();
  if (!text.trim()) return undefined as T;
  return JSON.parse(text) as T;
}
