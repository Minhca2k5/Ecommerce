import { ApiError } from "@/lib/apiError";

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

function shouldRedactIds(): boolean {
  try {
    return !(typeof window !== "undefined" && window.location?.pathname?.startsWith("/admin"));
  } catch {
    return true;
  }
}

function looksLikeLeakyIdMessage(msg: string): boolean {
  if (!msg.trim()) return false;
  if (/(^|[^\w])#\d+\b/.test(msg)) return true;
  if (/\bwith\s+id\s+\d+\b/i.test(msg)) return true;
  if (/\bwith\s+ID\s+\d+\b/i.test(msg)) return true;
  if (/\b(?:id|ID|cartId|cartItemId|productId|categoryId|orderId|userId|voucherId|paymentId|reviewId|addressId|notificationId)\b\s*(?:[:=#]|\bis\b|\bwas\b)?\s*\d+/.test(msg))
    return true;
  return false;
}

export function getErrorMessage(error: unknown, fallback: string) {
  const redact = shouldRedactIds();
  if (error instanceof ApiError) {
    const payload = error.payload;
    if (isRecord(payload) && typeof payload.message === "string" && payload.message.trim()) {
      if (!redact) return payload.message;
      return looksLikeLeakyIdMessage(payload.message) ? fallback : payload.message;
    }
    return fallback;
  }

  if (error instanceof Error && error.message.trim()) {
    if (!redact) return error.message;
    return looksLikeLeakyIdMessage(error.message) ? fallback : error.message;
  }
  return fallback;
}
