import { apiJson } from "@/lib/http";
import { buildQuery } from "@/lib/apiClient";

export type PageResponse<T> = {
  content: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
  first?: boolean;
  last?: boolean;
};

export async function adminGet<T>(path: string) {
  return apiJson<T>(path, { method: "GET", auth: true });
}

export async function adminPost<T>(path: string, body: unknown) {
  return apiJson<T>(path, { method: "POST", body, auth: true });
}

export async function adminPut<T>(path: string, body?: unknown) {
  return apiJson<T>(path, { method: "PUT", body, auth: true });
}

export async function adminPatch<T>(path: string, body?: unknown) {
  return apiJson<T>(path, { method: "PATCH", body, auth: true });
}

export async function adminDelete<T>(path: string) {
  return apiJson<T>(path, { method: "DELETE", auth: true });
}

export function withPage(params: { page: number; size: number; sort?: string }) {
  return buildQuery(params);
}

