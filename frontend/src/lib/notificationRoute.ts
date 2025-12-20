import type { NotificationResponse } from "@/lib/notificationApi";

export function getNotificationRoute(notification: NotificationResponse): string | null {
  const type = String(notification.type || "").toUpperCase();
  const referenceType = String(notification.referenceType || "").toUpperCase();
  const referenceId = notification.referenceId ?? notification.id;
  const id = referenceId !== undefined && referenceId !== null ? Number(referenceId) : 0;

  if (type === "ORDER" || type === "PAYMENT") {
    return id ? `/orders/${id}` : "/orders";
  }

  if (type === "VOUCHER") {
    return "/me/voucher-uses";
  }

  if (type === "REVIEW") {
    if (referenceType === "PRODUCT" && id) return `/products/${id}`;
    return "/me/reviews";
  }

  if (type === "PRODUCT") {
    return id ? `/products/${id}` : "/products";
  }

  if (type === "CATEGORY") {
    return id ? `/categories/${id}` : "/categories";
  }

  if (type === "SYSTEM") {
    return "/me";
  }

  return null;
}

