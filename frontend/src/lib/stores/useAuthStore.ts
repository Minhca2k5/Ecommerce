import { create } from "zustand";
import {
    type StoredTokens,
    getStoredTokens,
    setStoredTokens,
    clearStoredTokens,
} from "@/lib/authStorage";

type AuthUser = {
    id?: number;
    username?: string;
    email?: string;
    fullName?: string;
};

interface AuthState {
    user: AuthUser | null;
    tokens: StoredTokens | null;
    isAuthenticated: boolean;
    login: (tokens: StoredTokens) => void;
    logout: () => void;
    setUser: (user: AuthUser | null) => void;
}

export const useAuthStore = create<AuthState>((set) => ({
    user: null,
    tokens: getStoredTokens(),
    isAuthenticated: !!getStoredTokens(),
    login: (tokens) => {
        setStoredTokens(tokens);
        set({ tokens, isAuthenticated: true });
    },
    logout: () => {
        clearStoredTokens();
        set({ user: null, tokens: null, isAuthenticated: false });
    },
    setUser: (user) => set({ user }),
}));
