import { apiJson } from "@/lib/http";
import type { SpringPage } from "@/lib/pagination";

export type VoucherResponse = {
  id?: number;
  code?: string;
  name?: string;
  description?: string;
  discountType?: string;
  discountValue?: number | string;
  maxDiscountAmount?: number | string;
  minOrderTotal?: number | string;
  startAt?: string;
  endAt?: string;
};

export function getVouchersByCode(code: string) {
  return apiJson<VoucherResponse[]>(`/api/public/vouchers?code=${encodeURIComponent(code)}`, { method: "GET", auth: false });
}

export function filterVouchersByMinOrderAmount(minOrderAmount: number, params?: { page?: number; size?: number }) {
  const sp = new URLSearchParams();
  sp.set("minOrderAmount", String(minOrderAmount));
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  return apiJson<SpringPage<VoucherResponse>>(`/api/public/vouchers/filter?${sp.toString()}`, { method: "GET", auth: false });
}

export function getVoucherById(voucherId: number) {
  return apiJson<VoucherResponse>(`/api/public/vouchers/${voucherId}`, { method: "GET", auth: false });
}
