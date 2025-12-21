import { getStoredTokens } from "@/lib/authStorage";

const SELECTED_ROLE_KEY = "selectedRole";

function normalizeRole(role: string): string {
  const r = role.trim().toUpperCase();
  if (r === "ROLE_ADMIN" || r === "ADMIN" || r === "ROLE:ADMIN") return "ADMIN";
  if (r === "ROLE_USER" || r === "USER" || r === "ROLE:USER") return "USER";
  return r.replace(/^ROLE_/, "");
}

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

export function getAvailableRoles(): string[] {
  const tokens = getStoredTokens();
  if (!tokens?.accessToken) return [];
  const payload = decodeJwtPayload(tokens.accessToken);
  const roles = getRoleStrings(payload).map(normalizeRole).filter(Boolean);
  return Array.from(new Set(roles));
}

export function getSelectedRole(): string | null {
  const v = localStorage.getItem(SELECTED_ROLE_KEY);
  return v && v.trim() ? v : null;
}

export function setSelectedRole(role: string) {
  localStorage.setItem(SELECTED_ROLE_KEY, normalizeRole(role));
}

export function clearSelectedRole() {
  localStorage.removeItem(SELECTED_ROLE_KEY);
}

export function hasRole(role: string): boolean {
  const roles = getAvailableRoles();
  return roles.includes(normalizeRole(role));
}
