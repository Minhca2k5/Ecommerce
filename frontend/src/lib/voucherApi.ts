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
  usageLimitGlobal?: number;
  usageLimitUser?: number;
  activeUses?: number;
  activeUsesForUser?: number;
  startAt?: string;
  endAt?: string;
};

// User vouchers (requires ROLE_USER)
export function getMyVouchersByCode(code: string) {
  return apiJson<VoucherResponse[]>(`/api/users/me/vouchers?code=${encodeURIComponent(code)}`, { method: "GET", auth: true });
}

export function filterMyVouchersByMinOrderAmount(minOrderAmount: number, params?: { page?: number; size?: number }) {
  const sp = new URLSearchParams();
  sp.set("minOrderAmount", String(minOrderAmount));
  if (params?.page !== undefined) sp.set("page", String(params.page));
  if (params?.size !== undefined) sp.set("size", String(params.size));
  return apiJson<SpringPage<VoucherResponse>>(`/api/users/me/vouchers/filter?${sp.toString()}`, { method: "GET", auth: true });
}

export function getMyVoucherById(voucherId: number) {
  return apiJson<VoucherResponse>(`/api/users/me/vouchers/${voucherId}`, { method: "GET", auth: true });
}

// Backward-compatible exports (older code paths)
export const getVouchersByCode = getMyVouchersByCode;
export const filterVouchersByMinOrderAmount = filterMyVouchersByMinOrderAmount;
export const getVoucherById = getMyVoucherById;
