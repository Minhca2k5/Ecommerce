export type StoredTokens = {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
};

const ACCESS_TOKEN_KEY = "accessToken";
const REFRESH_TOKEN_KEY = "refreshToken";
const TOKEN_TYPE_KEY = "tokenType";

export function getStoredTokens(): StoredTokens | null {
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY) ?? "";
  const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY) ?? "";
  const tokenType = localStorage.getItem(TOKEN_TYPE_KEY) ?? "Bearer";
  if (!accessToken || !refreshToken) return null;
  return { accessToken, refreshToken, tokenType };
}

export function setStoredTokens(tokens: StoredTokens) {
  localStorage.setItem(ACCESS_TOKEN_KEY, tokens.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  localStorage.setItem(TOKEN_TYPE_KEY, tokens.tokenType || "Bearer");
}

export function clearStoredTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  localStorage.removeItem(TOKEN_TYPE_KEY);
}

