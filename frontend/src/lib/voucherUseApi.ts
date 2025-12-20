import { apiJson } from "@/lib/http";
import type { SpringPage } from "@/lib/pagination";

export type VoucherUseResponse = {
  id?: number;
  createdAt?: string;
  voucherId?: number;
  userId?: number;
  orderId?: number;
  discountAmount?: number | string;
};

export function listMyVoucherUses(params?: { page?: number; size?: number }) {
  const sp = new URLSearchParams();
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  const qs = sp.toString();
  return apiJson<SpringPage<VoucherUseResponse>>(`/api/users/me/voucher-uses/user/me${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

export function listMyVoucherUsesByOrder(orderId: number, params?: { page?: number; size?: number }) {
  const sp = new URLSearchParams();
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  const qs = sp.toString();
  return apiJson<SpringPage<VoucherUseResponse>>(`/api/users/me/voucher-uses/order/${orderId}${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

export function listMyVoucherUsesByVoucher(voucherId: number, params?: { page?: number; size?: number }) {
  const sp = new URLSearchParams();
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  const qs = sp.toString();
  return apiJson<SpringPage<VoucherUseResponse>>(`/api/users/me/voucher-uses/voucher/${voucherId}${qs ? `?${qs}` : ""}`, { method: "GET", auth: true });
}

