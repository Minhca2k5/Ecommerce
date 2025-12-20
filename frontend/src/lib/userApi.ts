import { apiJson } from "@/lib/http";

export type UserMeResponse = {
  id?: number;
  username?: string;
  email?: string;
  fullName?: string;
  phone?: string;
  enabled?: boolean;
};

export type UserUpdateRequest = {
  username?: string;
  email?: string;
  fullName?: string;
  phone?: string;
};

export type PasswordRequest = {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export type AddressResponse = {
  id?: number;
  userId?: number;
  line1?: string;
  line2?: string;
  city?: string;
  state?: string;
  country?: string;
  zipcode?: string;
  isDefault?: boolean;
};

export type AddressCreateRequest = {
  line1: string;
  line2?: string;
  city: string;
  state?: string;
  country: string;
  zipcode?: string;
  isDefault?: boolean;
};

export type AddressUpdateRequest = Partial<Omit<AddressCreateRequest, "isDefault">>;

export function getMe() {
  return apiJson<UserMeResponse>("/api/users/me", { method: "GET", auth: true });
}

export function getMeDetails() {
  return apiJson<UserMeResponse>("/api/users/me/details", { method: "GET", auth: true });
}

export function updateMe(request: UserUpdateRequest) {
  return apiJson<UserMeResponse>("/api/users/me", { method: "PUT", auth: true, body: request });
}

export function changePassword(request: PasswordRequest) {
  return apiJson<UserMeResponse>("/api/users/me/password", { method: "PATCH", auth: true, body: request });
}

export function deleteMe() {
  return apiJson<void>("/api/users/me", { method: "DELETE", auth: true });
}

export function listAddresses() {
  return apiJson<AddressResponse[]>("/api/users/me/addresses", { method: "GET", auth: true });
}

export function getDefaultAddress() {
  return apiJson<AddressResponse>("/api/users/me/addresses/default", { method: "GET", auth: true });
}

export function createAddress(request: AddressCreateRequest) {
  return apiJson<AddressResponse>("/api/users/me/addresses", { method: "POST", auth: true, body: request });
}

export function updateAddress(addressId: number, request: AddressUpdateRequest) {
  return apiJson<AddressResponse>(`/api/users/me/addresses/${addressId}`, { method: "PUT", auth: true, body: request });
}

export function deleteAddress(addressId: number) {
  return apiJson<void>(`/api/users/me/addresses/${addressId}`, { method: "DELETE", auth: true });
}

export function setDefaultAddress(addressId: number) {
  return apiJson<void>(`/api/users/me/addresses/${addressId}/set-default`, { method: "PATCH", auth: true });
}

export function getAddressById(addressId: number) {
  return apiJson<AddressResponse>(`/api/users/me/addresses/${addressId}`, { method: "GET", auth: true });
}
