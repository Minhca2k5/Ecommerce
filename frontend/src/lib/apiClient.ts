import { getApiBaseUrl } from "@/lib/env";

type ApiErrorPayload = unknown;

export class ApiError extends Error {
  status: number;
  payload?: ApiErrorPayload;

  constructor(message: string, status: number, payload?: ApiErrorPayload) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.payload = payload;
  }
}

async function parseJsonSafe(response: Response): Promise<ApiErrorPayload | undefined> {
  const contentType = response.headers.get("content-type") ?? "";
  if (!contentType.includes("application/json")) return undefined;
  try {
    return (await response.json()) as ApiErrorPayload;
  } catch {
    return undefined;
  }
}

export async function apiGet<T>(path: string, init?: RequestInit): Promise<T> {
  const url = `${getApiBaseUrl()}${path.startsWith("/") ? "" : "/"}${path}`;

  const response = await fetch(url, {
    ...init,
    method: "GET",
    headers: {
      Accept: "application/json",
      ...(init?.headers ?? {}),
    },
  });

  if (!response.ok) {
    const payload = await parseJsonSafe(response);
    const message = `Request failed: ${response.status} ${response.statusText}`;
    throw new ApiError(message, response.status, payload);
  }

  return (await response.json()) as T;
}

export function buildQuery(params: Record<string, string | number | boolean | null | undefined>) {
  const searchParams = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === null || value === undefined) continue;
    searchParams.set(key, String(value));
  }
  const query = searchParams.toString();
  return query ? `?${query}` : "";
}

export async function apiGetOrNull<T>(path: string, init?: RequestInit): Promise<T | null> {
  try {
    return await apiGet<T>(path, init);
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return null;
    throw e;
  }
}
