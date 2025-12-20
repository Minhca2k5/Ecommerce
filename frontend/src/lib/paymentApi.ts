import { apiJson } from "@/lib/http";

export type PaymentRequest = {
  orderId: number;
  method?: string;
  status?: string;
  providerTxnId?: string;
  voucherId?: number;
  discountAmount?: number;
};

export type PaymentResponse = {
  id?: number;
  orderId?: number;
  method?: string;
  amount?: number;
  status?: string;
  providerTxnId?: string;
  orderTotalAmount?: number;
  orderCurrency?: string;
  orderStatus?: string;
};

export function listPayments(orderId: number) {
  return apiJson<PaymentResponse[]>(`/api/users/me/orders/${orderId}/payments`, { method: "GET", auth: true });
}

export function createPayment(orderId: number, request: PaymentRequest) {
  return apiJson<PaymentResponse>(`/api/users/me/orders/${orderId}/payments`, { method: "POST", auth: true, body: request });
}

