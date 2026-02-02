import { useMemo } from "react";
import { useNavigate } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useNotifications } from "@/app/NotificationProvider";
import { getNotificationRoute } from "@/lib/notificationRoute";
import { acceptGroupInvite, declineGroupInvite } from "@/lib/chatbotApi";

function formatTime(iso: string) {
  try {
    const date = new Date(iso);
    return date.toLocaleString();
  } catch {
    return iso;
  }
}

function typeDot(type: string) {
  const cls =
    type === "PAYMENT"
      ? "bg-emerald-500"
      : type === "ORDER"
        ? "bg-blue-500"
        : type === "VOUCHER"
          ? "bg-fuchsia-500"
          : type === "REVIEW"
            ? "bg-amber-500"
            : type === "PRODUCT" || type === "CATEGORY"
              ? "bg-indigo-500"
              : "bg-primary";
  return <span className={`mt-1 h-2.5 w-2.5 shrink-0 rounded-full ${cls}`} />;
}

export default function NotificationsPage() {
  const navigate = useNavigate();
  const notifications = useNotifications();

  const sorted = useMemo(() => {
    const next = [...notifications.items].filter((n) => !n.isHidden);
    next.sort((a, b) => String(b.createdAt || "").localeCompare(String(a.createdAt || "")));
    return next;
  }, [notifications.items]);

  function isInviteNotification(n: { referenceType?: unknown; title?: unknown }) {
    return String(n.referenceType ?? "").toLowerCase() === "chat_group_invite"
      && String(n.title ?? "").toLowerCase() === "group invitation";
  }

  if (!sorted.length) {
    return (
      <EmptyState
        title="No notifications"
        description="You're all caught up."
        action={
          <Button className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={() => navigate("/")}>
            Go home
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Account</div>
            <div className="text-3xl font-semibold tracking-tight">Notifications</div>
            <div className="mt-1 text-sm text-muted-foreground">Click a notification to open the referenced page.</div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur" onClick={() => notifications.markAllRead()}>
              Mark all read
            </Button>
            <Button variant="outline" className="h-10 rounded-xl border-rose-500/20 bg-background/70 text-rose-700 hover:bg-rose-500/10 backdrop-blur" onClick={() => notifications.clear()}>
              Clear all
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-4 lg:grid-cols-2">
        {sorted.map((n) => (
          <Card
            key={String(n.id)}
            className={[
              "pressable relative overflow-hidden bg-background/70 backdrop-blur shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg",
              n.isRead ? "opacity-90 ring-1 ring-border/40" : "shine ring-1 ring-primary/25",
            ].join(" ")}
            onClick={() => {
              const id = Number(n.id ?? 0);
              if (id) notifications.markRead(id);
              const route = getNotificationRoute(n);
              if (route) navigate(route);
            }}
          >
            <div
              className={[
                "pointer-events-none absolute inset-0 opacity-25",
                n.isRead
                  ? "[background:radial-gradient(60%_60%_at_20%_20%,rgba(239,68,68,.12),transparent),radial-gradient(50%_60%_at_75%_40%,rgba(16,185,129,.08),transparent)]"
                  : "[background:radial-gradient(60%_60%_at_20%_20%,rgba(16,185,129,.14),transparent),radial-gradient(50%_60%_at_75%_40%,rgba(239,68,68,.10),transparent)]",
              ].join(" ")}
            />
            <CardHeader className="relative">
              <CardTitle className="flex items-start justify-between gap-3">
                <span className="flex min-w-0 items-start gap-2">
                  {typeDot(String(n.type ?? "SYSTEM"))}
                  <span className="min-w-0">
                    <span className="block truncate">{n.title}</span>
                    <span className="mt-1 block text-xs font-normal text-muted-foreground">{formatTime(String(n.createdAt ?? ""))}</span>
                  </span>
                </span>
                {!n.isRead ? (
                  <span className="rounded-full bg-emerald-500/10 px-2 py-1 text-[11px] text-emerald-700 ring-1 ring-emerald-500/20">
                    Unread
                  </span>
                ) : (
                  <span className="rounded-full bg-rose-500/10 px-2 py-1 text-[11px] text-rose-700 ring-1 ring-rose-500/20">Read</span>
                )}
              </CardTitle>
            </CardHeader>
            <CardContent className="relative">
              <div className={["text-sm", n.isRead ? "text-muted-foreground" : "text-foreground/90"].join(" ")}>
                {isInviteNotification(n) && n.isRead
                  ? "Lời mời này đã được xử lý."
                  : n.message}
              </div>
              <div className="mt-3 flex justify-between">
                <Button
                  type="button"
                  variant="outline"
                  className="rounded-xl bg-background/70 backdrop-blur"
                  onClick={(e) => {
                    e.stopPropagation();
                    const id = Number(n.id ?? 0);
                    if (id) notifications.remove(id);
                  }}
                >
                  Remove
                </Button>
                <div className="flex gap-2">
                  {isInviteNotification(n) && !n.isRead ? (
                    <>
                      <Button
                        type="button"
                        className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
                        onClick={async (e) => {
                          e.stopPropagation();
                          const id = Number(n.id ?? 0);
                          if (id) notifications.markRead(id);
                          try { await acceptGroupInvite(Number(n.referenceId)); } catch {}
                        }}
                      >
                        Accept
                      </Button>
                      <Button
                        type="button"
                        variant="outline"
                        className="h-10 rounded-xl border-rose-500/20 text-rose-700"
                        onClick={async (e) => {
                          e.stopPropagation();
                          const id = Number(n.id ?? 0);
                          if (id) notifications.markRead(id);
                          try { await declineGroupInvite(Number(n.referenceId)); } catch {}
                        }}
                      >
                        Refuse
                      </Button>
                    </>
                  ) : (
                    <Button
                      type="button"
                      className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
                      onClick={(e) => {
                        e.stopPropagation();
                        const id = Number(n.id ?? 0);
                        if (id) notifications.markRead(id);
                        const route = getNotificationRoute(n);
                        if (route) navigate(route);
                      }}
                    >
                      Open
                    </Button>
                  )}
                </div>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
