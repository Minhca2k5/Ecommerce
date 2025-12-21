import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { ApiError } from "@/lib/apiError";
import { getMe, login as loginApi, register as registerApi } from "@/lib/authApi";
import { clearStoredTokens, getStoredTokens, setStoredTokens, type StoredTokens } from "@/lib/authStorage";
import { clearSelectedRole } from "@/lib/roleSelection";

export type AuthUser = {
  id?: number;
  username?: string;
  email?: string;
  fullName?: string;
};

type AuthContextValue = {
  user: AuthUser | null;
  isReady: boolean;
  isAuthenticated: boolean;
  login: (username: string, password: string) => Promise<void>;
  register: (payload: { username: string; email: string; password: string; fullName?: string; phone?: string }) => Promise<void>;
  logout: () => void;
  refreshMe: () => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

function normalizeTokens(response: { accessToken?: string; refreshToken?: string; tokenType?: string }): StoredTokens | null {
  if (!response.accessToken || !response.refreshToken) return null;
  return { accessToken: response.accessToken, refreshToken: response.refreshToken, tokenType: response.tokenType || "Bearer" };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isReady, setIsReady] = useState(false);

  const refreshMe = useCallback(async () => {
    try {
      const me = await getMe();
      setUser({ id: me.id, username: me.username, email: me.email, fullName: me.fullName });
    } catch (e) {
      if (e instanceof ApiError && (e.status === 401 || e.status === 403)) {
        clearStoredTokens();
        clearSelectedRole();
        setUser(null);
        return;
      }
      throw e;
    }
  }, []);

  useEffect(() => {
    const tokens = getStoredTokens();
    if (!tokens) {
      setIsReady(true);
      return;
    }
    refreshMe()
      .catch(() => {
        clearStoredTokens();
        setUser(null);
      })
      .finally(() => setIsReady(true));
  }, [refreshMe]);

  const login = useCallback(async (username: string, password: string) => {
    const response = await loginApi({ username, password });
    const tokens = normalizeTokens(response);
    if (!tokens) throw new Error(response.message || "Login failed");
    setStoredTokens(tokens);
    await refreshMe();
  }, [refreshMe]);

  const register = useCallback(
    async (payload: { username: string; email: string; password: string; fullName?: string; phone?: string }) => {
      const response = await registerApi(payload);

      const directTokens = normalizeTokens(response);
      if (directTokens) {
        setStoredTokens(directTokens);
        await refreshMe();
        return;
      }

      const loginResponse = await loginApi({ username: payload.username, password: payload.password });
      const loginTokens = normalizeTokens(loginResponse);
      if (!loginTokens) throw new Error(loginResponse.message || response.message || "Register failed");

      setStoredTokens(loginTokens);
      await refreshMe();
    },
    [refreshMe],
  );

  const logout = useCallback(() => {
    clearStoredTokens();
    clearSelectedRole();
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isReady,
      isAuthenticated: Boolean(getStoredTokens()?.accessToken),
      login,
      register,
      logout,
      refreshMe,
    }),
    [isReady, login, logout, refreshMe, register, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
