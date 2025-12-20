import { useEffect, useMemo, useRef, useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { getNotificationRoute } from "@/lib/notificationRoute";

const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "rounded-full bg-primary/10 px-3 py-1.5 text-foreground ring-1 ring-primary/20"
    : "rounded-full px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground";

export default function AppLayout() {
  const auth = useAuth();
  const toast = useToast();
  const notifications = useNotifications();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);
  const notifRef = useRef<HTMLDivElement | null>(null);

  const displayName = useMemo(
    () => auth.user?.fullName || auth.user?.username || auth.user?.email || "Account",
    [auth.user?.email, auth.user?.fullName, auth.user?.username],
  );

  useEffect(() => {
    if (!isMenuOpen && !isNotifOpen) return;
    function onPointerDown(e: PointerEvent) {
      const target = e.target as Node | null;
      if (!target) return;
      if (menuRef.current?.contains(target)) return;
      if (notifRef.current?.contains(target)) return;
      setIsMenuOpen(false);
      setIsNotifOpen(false);
    }
    window.addEventListener("pointerdown", onPointerDown);
    return () => window.removeEventListener("pointerdown", onPointerDown);
  }, [isMenuOpen, isNotifOpen]);

  return (
    <div className="relative min-h-dvh flex flex-col overflow-hidden bg-background">
      <div className="pointer-events-none absolute inset-0 animated-aurora opacity-70" />
      <header className="sticky top-0 z-50 border-b bg-background/75 backdrop-blur supports-[backdrop-filter]:bg-background/50">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to="/" className="font-semibold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500">
            Ecommerce
          </Link>

          <nav className="flex items-center gap-4 text-sm">
            <NavLink to="/" end className={navLinkClassName}>
              <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M3 10.5l9-7 9 7" />
                  <path d="M5 10v10h14V10" />
                </svg>
                Home
              </span>
            </NavLink>
            <NavLink to="/categories" className={navLinkClassName}>
              <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M4 6h7v7H4z" />
                  <path d="M13 6h7v7h-7z" />
                  <path d="M4 15h7v5H4z" />
                  <path d="M13 15h7v5h-7z" />
                </svg>
                Categories
              </span>
            </NavLink>
            <NavLink to="/products" className={navLinkClassName}>
              <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                  <path d="M6 7h12l-1 13H7L6 7z" />
                  <path d="M9 7a3 3 0 0 1 6 0" />
                </svg>
                Products
              </span>
            </NavLink>
            {auth.isAuthenticated ? (
              <>
                <NavLink to="/cart" className={navLinkClassName}>
                  <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                    <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M6 7h12l-1 13H7L6 7z" />
                      <path d="M9 7a3 3 0 0 1 6 0" />
                    </svg>
                    Cart
                  </span>
                </NavLink>

                <div ref={notifRef} className="relative">
                  <button
                    type="button"
                    onClick={() => {
                      setIsMenuOpen(false);
                      setIsNotifOpen((v) => !v);
                    }}
                    className="relative rounded-full px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground ring-1 ring-transparent hover:ring-primary/10"
                    aria-label="Notifications"
                  >
                    <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                      <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M18 8a6 6 0 10-12 0c0 7-3 7-3 7h18s-3 0-3-7" />
                        <path d="M13.73 21a2 2 0 01-3.46 0" />
                      </svg>
                      Alerts
                    </span>
                    {notifications.unreadCount ? (
                      <span className="absolute -right-0.5 -top-0.5 grid h-5 min-w-5 place-items-center rounded-full bg-rose-500 px-1 text-[10px] font-semibold text-white">
                        {notifications.unreadCount > 99 ? "99+" : String(notifications.unreadCount)}
                      </span>
                    ) : null}
                  </button>

                  {isNotifOpen ? (
                    <div className="absolute right-0 mt-2 w-80 overflow-hidden rounded-2xl border bg-background/80 shadow-lg backdrop-blur animate-in fade-in zoom-in-95">
                      <div className="flex items-center justify-between gap-2 p-3">
                        <div className="text-sm font-medium">Notifications</div>
                        <button
                          type="button"
                          className="rounded-lg px-2 py-1 text-xs text-muted-foreground hover:bg-muted hover:text-foreground"
                          onClick={() => notifications.markAllRead()}
                        >
                          Mark all read
                        </button>
                      </div>
                      <div className="max-h-80 overflow-auto border-t">
                        {(notifications.items ?? []).filter((n) => !n.isHidden).slice(0, 6).map((n) => (
                          <button
                            key={String(n.id)}
                            type="button"
                            className={[
                              "w-full px-3 py-3 text-left transition hover:bg-muted",
                              !n.isRead ? "bg-primary/5" : "",
                            ].join(" ")}
                            onClick={() => {
                              const id = Number(n.id ?? 0);
                              if (id) notifications.markRead(id);
                              setIsNotifOpen(false);
                              const route = getNotificationRoute(n);
                              navigate(route || "/notifications");
                            }}
                          >
                            <div className="text-sm font-medium truncate">{n.title}</div>
                            <div className="mt-1 text-xs text-muted-foreground line-clamp-2">{n.message}</div>
                          </button>
                        ))}
                        {(notifications.items ?? []).filter((n) => !n.isHidden).length === 0 ? (
                          <div className="p-3 text-sm text-muted-foreground">No notifications yet.</div>
                        ) : null}
                      </div>
                      <div className="border-t p-2">
                        <button
                          type="button"
                          className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          onClick={() => {
                            setIsNotifOpen(false);
                            navigate("/notifications");
                          }}
                        >
                          View all
                        </button>
                      </div>
                    </div>
                  ) : null}
                </div>
              </>
            ) : null}

            {!auth.isAuthenticated ? (
              <NavLink to="/login" className={navLinkClassName}>
                <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M15 3h4a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2h-4" />
                    <path d="M10 17l5-5-5-5" />
                    <path d="M15 12H3" />
                  </svg>
                  Login
                </span>
              </NavLink>
            ) : (
              <div ref={menuRef} className="relative">
                <button
                  type="button"
                  onClick={() => setIsMenuOpen((v) => !v)}
                  className="rounded-full px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground ring-1 ring-transparent hover:ring-primary/10"
                >
                  <span className="inline-flex items-center gap-2 transition hover:-translate-y-0.5">
                    <span className="grid h-6 w-6 place-items-center rounded-full bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-[10px] font-semibold text-white">
                      {(displayName || "A").slice(0, 1).toUpperCase()}
                    </span>
                    <span className="max-w-[10rem] truncate">{displayName}</span>
                    <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M6 9l6 6 6-6" />
                    </svg>
                  </span>
                </button>

                {isMenuOpen ? (
                  <div
                    className="absolute right-0 mt-2 w-64 overflow-hidden rounded-2xl border bg-background/80 shadow-lg backdrop-blur animate-in fade-in zoom-in-95"
                    onMouseLeave={() => setIsMenuOpen(false)}
                  >
                    <div className="p-3">
                      <div className="text-sm font-medium truncate">{displayName}</div>
                      {auth.user?.email ? <div className="text-xs text-muted-foreground truncate">{auth.user.email}</div> : null}
                    </div>
                    <div className="border-t p-2">
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/me");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Profile
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/me/addresses");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Addresses
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/me/wishlist");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Wishlist
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/orders");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Orders
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/me/voucher-uses");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Voucher uses
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate("/me/search-logs");
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Search logs
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          auth.logout();
                          setIsMenuOpen(false);
                          toast.push({ variant: "success", title: "Logged out", message: "See you again soon." });
                          navigate("/", { replace: true });
                        }}
                        className="w-full rounded-xl px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Logout
                      </button>
                    </div>
                  </div>
                ) : null}
              </div>
            )}
          </nav>
        </div>
      </header>

      <main className="flex-1">
        <div className="mx-auto max-w-6xl px-4 py-6">
          <Outlet />
        </div>
      </main>

      <footer className="border-t">
        <div className="mx-auto max-w-6xl px-4 py-6 text-sm text-muted-foreground">© {new Date().getFullYear()} Ecommerce</div>
      </footer>
    </div>
  );
}
