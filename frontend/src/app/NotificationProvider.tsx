import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";
import { useAuth } from "@/app/AuthProvider";
import { createMyNotification, listMyNotifications, setAllNotificationsStatus, setNotificationHidden, setNotificationRead, type NotificationCreateRequest, type NotificationResponse } from "@/lib/notificationApi";
import { ApiError } from "@/lib/apiError";

type CreateNotificationInput = {
  type: string;
  title: string;
  message: string;
  referenceId?: string | number;
  referencePath?: string;
  referenceType?: string;
};

type NotificationContextValue = {
  items: NotificationResponse[];
  unreadCount: number;
  push: (input: CreateNotificationInput) => void;
  markRead: (id: number) => void;
  markAllRead: () => void;
  remove: (id: number) => void;
  clear: () => void;
  refresh: () => Promise<void>;
};

const NotificationContext = createContext<NotificationContextValue | null>(null);

export function NotificationProvider({ children }: { children: React.ReactNode }) {
  const auth = useAuth();
  const userId = useMemo(() => auth.user?.id, [auth.user?.id]);
  const [items, setItems] = useState<NotificationResponse[]>([]);

  useEffect(() => {
    if (!auth.isAuthenticated) {
      setItems([]);
      return;
    }
    listMyNotifications()
      .then((data) => setItems((data ?? []).filter((n) => !n.isHidden)))
      .catch(() => setItems([]));
  }, [auth.isAuthenticated]);

  const refresh = useCallback(async () => {
    if (!auth.isAuthenticated) return;
    const data = await listMyNotifications();
    setItems((data ?? []).filter((n) => !n.isHidden));
  }, [auth.isAuthenticated]);

  const push = useCallback(
    (input: CreateNotificationInput) => {
      if (!auth.isAuthenticated || !userId) return;
      const request: NotificationCreateRequest = {
        userId,
        title: input.title,
        message: input.message,
        type: input.type,
        referenceId: input.referenceId !== undefined ? Number(input.referenceId) : undefined,
        referenceType: input.referenceType ?? (input.referencePath ? "url" : undefined),
      };
      createMyNotification(request)
        .then(() => refresh())
        .catch(() => {
          // ignore
        });
    },
    [auth.isAuthenticated, refresh, userId],
  );

  const markRead = useCallback(
    (id: number) => {
      if (!auth.isAuthenticated) return;
      setItems((prev) => prev.map((n) => (Number(n.id) === id ? { ...n, isRead: true } : n)));
      setNotificationRead(id, true).catch(() => {
        // ignore
      });
    },
    [auth.isAuthenticated],
  );

  const markAllRead = useCallback(() => {
    if (!auth.isAuthenticated) return;
    setItems((prev) => prev.map((n) => ({ ...n, isRead: true })));
    setAllNotificationsStatus({ isRead: true }).catch(() => {
      // ignore
    });
  }, [auth.isAuthenticated]);

  const remove = useCallback(
    (id: number) => {
      if (!auth.isAuthenticated) return;
      setItems((prev) => prev.filter((n) => Number(n.id) !== id));
      setNotificationHidden(id, true).catch(() => {
        // ignore
      });
    },
    [auth.isAuthenticated],
  );

  const clear = useCallback(() => {
    if (!auth.isAuthenticated) return;
    setItems([]);
    setAllNotificationsStatus({ isHidden: true }).catch((e) => {
      if (e instanceof ApiError && e.status === 400) return;
    });
  }, [auth.isAuthenticated]);

  const unreadCount = useMemo(() => items.filter((n) => !n.isRead && !n.isHidden).length, [items]);

  const value = useMemo<NotificationContextValue>(
    () => ({ items, unreadCount, push, markRead, markAllRead, remove, clear, refresh }),
    [clear, items, markAllRead, markRead, push, refresh, remove, unreadCount],
  );

  return <NotificationContext.Provider value={value}>{children}</NotificationContext.Provider>;
}

export function useNotifications() {
  const ctx = useContext(NotificationContext);
  if (!ctx) throw new Error("useNotifications must be used within NotificationProvider");
  return ctx;
}
