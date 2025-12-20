import { apiJson } from "@/lib/http";

export type SearchLogResponse = {
  id?: number;
  createdAt?: string;
  userId?: number;
  keyword?: string;
};

export type SearchLogRequest = {
  keyword: string;
};

export function listMySearchLogs(params?: { keyword?: string; page?: number; size?: number }) {
  const sp = new URLSearchParams();
  if (params?.keyword?.trim()) sp.set("keyword", params.keyword.trim());
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  const qs = sp.toString();
  return apiJson<SearchLogResponse[]>(`/api/users/me/search-logs${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

export function addSearchLog(request: SearchLogRequest) {
  return apiJson<SearchLogResponse>("/api/users/me/search-logs", { method: "POST", auth: true, body: request });
}

export function deleteAllSearchLogs() {
  return apiJson<void>("/api/users/me/search-logs", { method: "DELETE", auth: true });
}

export function deleteSearchLog(searchLogId: number) {
  return apiJson<void>(`/api/users/me/search-logs/${searchLogId}`, { method: "DELETE", auth: true });
}

