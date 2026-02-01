import { ApiError } from "@/lib/apiError";
import { apiJson } from "@/lib/http";

export type CartItemResponse = {
  id?: number;
  cartId?: number;
  productId?: number;
  quantity?: number;
  lineTotal?: number;
  unitPriceSnapshot?: number;
  productName?: string;
  productSlug?: string;
  productSku?: string;
  productPrice?: number;
  productCurrency?: string;
  productStatus?: string;
  url?: string;
};

export type CartResponse = {
  id?: number;
  userId?: number;
  username?: string;
  fullName?: string;
  guestId?: string;
  items?: CartItemResponse[];
  itemCount?: number;
  totalQuantity?: number;
  itemsSubtotal?: number;
  discount?: number;
  shippingFee?: number;
  totalAmount?: number;
  currency?: string;
};

export type CartItemRequest = {
  cartId: number;
  productId: number;
  quantity: number; // delta (add/return), not absolute
};

export type SpringPage<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first?: boolean;
  last?: boolean;
};

export async function getMyCart(): Promise<CartResponse> {
  return apiJson<CartResponse>("/api/users/me/carts", { method: "GET", auth: true });
}

export async function createMyCart(): Promise<CartResponse> {
  return apiJson<CartResponse>("/api/users/me/carts", { method: "POST", auth: true });
}

export async function getOrCreateCart(): Promise<CartResponse> {
  try {
    return await getMyCart();
  } catch (e) {
    if (e instanceof ApiError && e.status === 404) return createMyCart();
    throw e;
  }
}

export async function listMyCartItems(cartId: number, params?: { page?: number; size?: number; productName?: string }) {
  const searchParams = new URLSearchParams();
  if (params?.page !== undefined) searchParams.set("page", String(params.page));
  if (params?.size !== undefined) searchParams.set("size", String(params.size));
  if (params?.productName?.trim()) searchParams.set("productName", params.productName.trim());
  const query = searchParams.toString();
  return apiJson<SpringPage<CartItemResponse>>(`/api/users/me/carts/${cartId}/items${query ? `?${query}` : ""}`, {
    method: "GET",
    auth: true,
  });
}

export async function addOrUpdateCartItem(request: CartItemRequest) {
  return apiJson<CartItemResponse>(`/api/users/me/carts/${request.cartId}/items`, { method: "POST", auth: true, body: request });
}

export async function returnUpdateCartItem(request: CartItemRequest) {
  return apiJson<CartItemResponse>(`/api/users/me/carts/${request.cartId}/items/return`, { method: "PUT", auth: true, body: request });
}

export async function deleteCartItem(cartId: number, cartItemId: number) {
  return apiJson<void>(`/api/users/me/carts/${cartId}/items/${cartItemId}`, { method: "DELETE", auth: true });
}

export async function clearCart(cartId: number) {
  return apiJson<void>(`/api/users/me/carts/${cartId}/items`, { method: "DELETE", auth: true });
}

export async function mergeGuestCart(guestId: string): Promise<CartResponse> {
  return apiJson<CartResponse>(`/api/users/me/carts/merge?guestId=${encodeURIComponent(guestId)}`, {
    method: "POST",
    auth: true,
  });
}

export const GUEST_CART_KEY = "guestCartId";

export function getStoredGuestId() {
  if (typeof window === "undefined") return null;
  return window.localStorage.getItem(GUEST_CART_KEY);
}

export function setStoredGuestId(guestId: string) {
  if (typeof window === "undefined") return;
  window.localStorage.setItem(GUEST_CART_KEY, guestId);
}

export function clearStoredGuestId() {
  if (typeof window === "undefined") return;
  window.localStorage.removeItem(GUEST_CART_KEY);
}

export async function createGuestCart(guestId?: string | null): Promise<CartResponse> {
  return apiJson<CartResponse>("/api/public/carts/guest", {
    method: "POST",
    auth: false,
    body: guestId ? { guestId } : undefined,
  });
}

export async function getGuestCart(guestId: string): Promise<CartResponse> {
  return apiJson<CartResponse>(`/api/public/carts/guest/${guestId}`, { method: "GET", auth: false });
}

export async function getOrCreateGuestCart(): Promise<CartResponse> {
  const stored = getStoredGuestId();
  if (stored) {
    try {
      return await getGuestCart(stored);
    } catch (e) {
      if (e instanceof ApiError && e.status === 404) {
        clearStoredGuestId();
      } else {
        throw e;
      }
    }
  }
  const created = await createGuestCart(null);
  if (created.guestId) setStoredGuestId(created.guestId);
  return created;
}

export async function listGuestCartItems(guestId: string): Promise<CartItemResponse[]> {
  return apiJson<CartItemResponse[]>(`/api/public/carts/guest/${guestId}/items`, { method: "GET", auth: false });
}

export async function addOrUpdateGuestCartItem(guestId: string, productId: number, quantity: number) {
  return apiJson<CartItemResponse>(`/api/public/carts/guest/${guestId}/items`, {
    method: "POST",
    auth: false,
    body: { productId, quantity },
  });
}

export async function deleteGuestCartItem(guestId: string, cartItemId: number) {
  return apiJson<void>(`/api/public/carts/guest/${guestId}/items/${cartItemId}`, { method: "DELETE", auth: false });
}

export async function clearGuestCart(guestId: string) {
  return apiJson<void>(`/api/public/carts/guest/${guestId}/items`, { method: "DELETE", auth: false });
}
