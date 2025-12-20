import { apiJson } from "@/lib/http";
import type { SpringPage } from "@/lib/pagination";

export type WishlistResponse = {
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

export type WishlistRequest = {
  productId: number;
};

export function listMyWishlists(params?: { page?: number; size?: number; productName?: string }) {
  const sp = new URLSearchParams();
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  if (params?.productName?.trim()) sp.set("productName", params.productName.trim());
  const qs = sp.toString();
  return apiJson<SpringPage<WishlistResponse>>(`/api/users/me/wishlists${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

export function addWishlist(request: WishlistRequest) {
  return apiJson<WishlistResponse>("/api/users/me/wishlists", { method: "POST", auth: true, body: request });
}

export function clearWishlist() {
  return apiJson<void>("/api/users/me/wishlists", { method: "DELETE", auth: true });
}

export function removeWishlist(wishlistId: number) {
  return apiJson<void>(`/api/users/me/wishlists/${wishlistId}`, { method: "DELETE", auth: true });
}

