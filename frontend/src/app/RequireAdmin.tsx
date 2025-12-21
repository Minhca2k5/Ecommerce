import { Outlet } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { getStoredTokens } from "@/lib/authStorage";
import { getSelectedRole } from "@/lib/roleSelection";

function decodeJwtPayload(token: string): unknown {
  try {
    const parts = token.split(".");
    if (parts.length < 2) return null;
    const base64 = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const json = decodeURIComponent(
      atob(base64)
        .split("")
        .map((c) => `%${`00${c.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join(""),
    );
    return JSON.parse(json) as unknown;
  } catch {
    return null;
  }
}

function getRoleStrings(payload: unknown): string[] {
  if (!payload || typeof payload !== "object") return [];
  const p = payload as Record<string, unknown>;
  const candidates = [p["roles"], p["authorities"], p["scope"], p["scp"]];
  const roles: string[] = [];
  for (const c of candidates) {
    if (Array.isArray(c)) {
      for (const v of c) if (typeof v === "string") roles.push(v);
    } else if (typeof c === "string") {
      roles.push(...c.split(/[,\s]+/).filter(Boolean));
    }
  }
  return roles;
}

function hasAdminRole(): boolean {
  const tokens = getStoredTokens();
  if (!tokens?.accessToken) return false;
  const payload = decodeJwtPayload(tokens.accessToken);
  const roles = getRoleStrings(payload).map((r) => r.toUpperCase());
  return roles.includes("ROLE_ADMIN") || roles.includes("ADMIN") || roles.includes("ROLE:ADMIN");
}

export default function RequireAdmin() {
  const auth = useAuth();
  const selectedRole = getSelectedRole();

  if (!auth.isReady) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (!auth.isAuthenticated) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-10">
        <EmptyState title="Admin only" description="Please sign in." />
      </div>
    );
  }

  if (!hasAdminRole()) {
    return (
      <div className="mx-auto max-w-3xl px-4 py-10">
        <EmptyState title="Admin only" description="You don’t have access to the admin dashboard." />
      </div>
    );
  }

  if (selectedRole && selectedRole.toUpperCase() !== "ADMIN") {
    return (
      <div className="mx-auto max-w-3xl px-4 py-10">
        <EmptyState title="Admin only" description="You selected USER mode. Switch role to access the admin dashboard." />
      </div>
    );
  }

  return <Outlet />;
}
