import { apiJson } from "@/lib/http";

export type MomoCreateResponse = {
  resultCode?: number;
  message?: string;
  payUrl?: string;
  deeplink?: string;
  qrCodeUrl?: string;
  requestId?: string;
  orderId?: string;
};

export async function createMomoPayment(orderId: number, idempotencyKey?: string) {
  return apiJson<MomoCreateResponse>(`/api/users/me/orders/${orderId}/payments/momo/create`, {
    method: "POST",
    auth: true,
    headers: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : undefined,
  });
}

export async function createGuestMomoPayment(orderId: number, accessToken: string, idempotencyKey?: string) {
  const q = new URLSearchParams({ accessToken }).toString();
  return apiJson<MomoCreateResponse>(`/api/public/guest/orders/${orderId}/payments/momo/create?${q}`, {
    method: "POST",
    auth: false,
    headers: idempotencyKey ? { "Idempotency-Key": idempotencyKey } : undefined,
  });
}
