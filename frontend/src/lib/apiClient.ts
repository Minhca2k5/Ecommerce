import { apiJson } from "@/lib/http";
import { ApiError } from "@/lib/apiError";

export async function apiGet<T>(path: string, init?: RequestInit): Promise<T> {
  if (init?.method && init.method !== "GET") {
    throw new Error("apiGet only supports GET");
  }
  return apiJson<T>(path, { method: "GET", headers: init?.headers, signal: init?.signal ?? undefined, auth: false });
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
