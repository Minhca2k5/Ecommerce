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
