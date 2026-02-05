import { apiJson } from "@/lib/http";

export type OrderRequest = {
  cartId: number;
  addressIdSnapshot: number;
  voucherId?: number;
  shippingFee?: number;
  currency?: string;
  status?: string;
};

export type OrderItemResponse = {
  id?: number;
  orderId?: number;
  productId?: number;
  productName?: string;
  productSlug?: string;
  unitPriceSnapshot?: number;
  quantity?: number;
  lineTotal?: number;
  url?: string;
};

export type OrderResponse = {
  id?: number;
  userId?: number;
  username?: string;
  fullName?: string;
  addressIdSnapshot?: number;
  voucherId?: number;
  discountAmount?: number;
  subtotalAmount?: number;
  shippingFee?: number;
  taxAmount?: number;
  totalAmount?: number;
  currency?: string;
  status?: string;
  items?: OrderItemResponse[];
  itemCount?: number;
};

export type OrderResponseAfterCreating = OrderResponse & {
  // POST response may include extra computed fields
};

export function listMyOrders() {
  return apiJson<OrderResponse[]>("/api/users/me/orders", { method: "GET", auth: true });
}

export function getMyOrder(orderId: number) {
  return apiJson<OrderResponse>(`/api/users/me/orders/${orderId}`, { method: "GET", auth: true });
}

export function createMyOrder(request: OrderRequest, idempotencyKey?: string) {
  return apiJson<OrderResponseAfterCreating>("/api/users/me/orders", {
    method: "POST",
    auth: true,
    body: request,
    headers: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : undefined,
  });
}

export function createGuestOrder(guestId: string, request: OrderRequest, idempotencyKey?: string) {
  return apiJson<OrderResponseAfterCreating>(`/api/public/checkout/guest/${encodeURIComponent(guestId)}`, {
    method: "POST",
    auth: false,
    body: request,
    headers: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : undefined,
  });
}

export function getMyVoucherDiscount(request: OrderRequest) {
  // Backend endpoint calculates discount amount for an order draft (voucher preview).
  // Using POST because browsers do not send request bodies with GET.
  return apiJson<number>("/api/users/me/orders/voucher-discount", { method: "POST", auth: true, body: request });
}
