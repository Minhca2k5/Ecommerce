import { apiJson } from "@/lib/http";

export type NotificationResponse = {
  id?: number;
  createdAt?: string;
  updatedAt?: string;

  userId?: number;
  title?: string;
  message?: string;
  type?: string;
  referenceId?: number;
  referenceType?: string;
  referenceUrl?: string;
  isRead?: boolean;
  isHidden?: boolean;
};

export type NotificationCreateRequest = {
  userId: number;
  title: string;
  message: string;
  type: string;
  referenceId?: number;
  referenceType?: string;
};

export function listMyNotifications() {
  return apiJson<NotificationResponse[]>("/api/users/me/notifications", { method: "GET", auth: true });
}

export function createMyNotification(request: NotificationCreateRequest) {
  return apiJson<NotificationResponse>("/api/users/me/notifications", { method: "POST", auth: true, body: request });
}

export function setNotificationRead(notificationId: number, isRead: boolean) {
  return apiJson<void>(`/api/users/me/notifications/${notificationId}/read?isRead=${encodeURIComponent(String(isRead))}`, { method: "PUT", auth: true });
}

export function setNotificationHidden(notificationId: number, isHidden: boolean) {
  return apiJson<void>(`/api/users/me/notifications/${notificationId}/hidden?isHidden=${encodeURIComponent(String(isHidden))}`, { method: "PUT", auth: true });
}

export function setAllNotificationsStatus(params: { isRead?: boolean; isHidden?: boolean }) {
  const search = new URLSearchParams();
  if (params.isRead !== undefined) search.set("isRead", String(params.isRead));
  if (params.isHidden !== undefined) search.set("isHidden", String(params.isHidden));
  const qs = search.toString();
  return apiJson<void>(`/api/users/me/notifications/user${qs ? `?${qs}` : ""}`, { method: "PUT", auth: true });
}

