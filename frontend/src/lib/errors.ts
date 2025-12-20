import { ApiError } from "@/lib/apiError";

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

export function getErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiError) {
    const payload = error.payload;
    if (isRecord(payload) && typeof payload.message === "string" && payload.message.trim()) return payload.message;
    return fallback;
  }

  if (error instanceof Error && error.message.trim()) return error.message;
  return fallback;
}

