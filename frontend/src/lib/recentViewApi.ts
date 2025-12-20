import { apiJson } from "@/lib/http";
import type { SpringPage } from "@/lib/pagination";

export type RecentViewResponse = {
  id?: number;
  createdAt?: string;
  userId?: number;
  productId?: number;
  productName?: string;
  productSlug?: string;
  productSku?: string;
  productPrice?: number | string;
  productCurrency?: string;
  productStatus?: string;
  url?: string;
};

export type RecentViewRequest = {
  productId: number;
};

export function listMyRecentViews(params?: { page?: number; size?: number; productName?: string }) {
  const sp = new URLSearchParams();
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  if (params?.productName?.trim()) sp.set("productName", params.productName.trim());
  const qs = sp.toString();
  return apiJson<SpringPage<RecentViewResponse>>(`/api/users/me/recent-views${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

export function addRecentView(request: RecentViewRequest) {
  return apiJson<RecentViewResponse>("/api/users/me/recent-views", { method: "POST", auth: true, body: request });
}

export function deleteRecentView(recentViewId: number) {
  return apiJson<void>(`/api/users/me/recent-views/${recentViewId}`, { method: "DELETE", auth: true });
}

export function clearRecentViews() {
  return apiJson<void>("/api/users/me/recent-views", { method: "DELETE", auth: true });
}

