import { apiJson } from "@/lib/http";

export type AuthResponse = {
  message?: string;
  accessToken?: string;
  refreshToken?: string;
  tokenType?: string;
};

export type LoginRequest = {
  username: string;
  password: string;
};

export type RegisterRequest = {
  username: string;
  email: string;
  password: string;
  fullName?: string;
  phone?: string;
};

export type RefreshTokenRequest = {
  refreshToken: string;
};

export type UserMeResponse = {
  username?: string;
  email?: string;
  fullName?: string;
  phone?: string;
  enabled?: boolean;
};

export async function login(request: LoginRequest) {
  return apiJson<AuthResponse>("/api/auth/login", { method: "POST", body: request, auth: false });
}

export async function register(request: RegisterRequest) {
  return apiJson<AuthResponse>("/api/auth/register", { method: "POST", body: request, auth: false });
}

export async function refreshToken(request: RefreshTokenRequest) {
  return apiJson<AuthResponse>("/api/auth/refresh-token", { method: "POST", body: request, auth: false });
}

export async function getMe() {
  return apiJson<UserMeResponse>("/api/users/me", { method: "GET", auth: true });
}

