import { useEffect, useMemo, useRef, useState } from "react";
import { Link, NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { getNotificationRoute } from "@/lib/notificationRoute";
import { acceptGroupInvite, declineGroupInvite } from "@/lib/chatbotApi";
import { getErrorMessage } from "@/lib/errors";
import { getAvailableRoles, getSelectedRole, setSelectedRole } from "@/lib/roleSelection";
import { getMyCart, getGuestCart, getStoredGuestId } from "@/lib/cartApi";
import ChatbotWidget from "@/app/ChatbotWidget";
import { cn } from "@/lib/utils";

const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "rounded-md bg-primary px-3 py-1.5 text-primary-foreground font-semibold"
    : "rounded-md px-3 py-1.5 text-white/80 hover:bg-white/10 hover:text-white";

export default function AppLayout() {
  const auth = useAuth();
  const toast = useToast();
  const notifications = useNotifications();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isNotifOpen, setIsNotifOpen] = useState(false);
  const [cartCount, setCartCount] = useState(0);
  const [searchQuery, setSearchQuery] = useState("");
  const menuRef = useRef<HTMLDivElement | null>(null);
  const notifRef = useRef<HTMLDivElement | null>(null);
  const isAdminRoute = location.pathname === "/admin" || location.pathname.startsWith("/admin/");

  const displayName = useMemo(
    () => auth.user?.fullName || auth.user?.username || auth.user?.email || "Account",
    [auth.user?.email, auth.user?.fullName, auth.user?.username],
  );

  const availableRoles = useMemo(() => getAvailableRoles(), [auth.isAuthenticated]);
  const selectedRole = useMemo(() => getSelectedRole(), [auth.isAuthenticated]);
  const [roleDraft, setRoleDraft] = useState(() => selectedRole ?? availableRoles[0] ?? "USER");

  function isInviteNotification(n: { referenceType?: unknown; title?: unknown }) {
    return String(n.referenceType ?? "").toLowerCase() === "chat_group_invite"
      && String(n.title ?? "").toLowerCase() === "group invitation";
  }

  useEffect(() => {
    setRoleDraft(selectedRole ?? availableRoles[0] ?? "USER");
  }, [availableRoles, selectedRole]);

  function defaultRouteForRole(role: string) {
    return role.toUpperCase() === "ADMIN" ? "/admin" : "/";
  }

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

  useEffect(() => {
    let cancelled = false;
    async function loadCartCount() {
      try {
        if (isAdminRoute) return;
        if (auth.isAuthenticated) {
          const cart = await getMyCart();
          const count = Number(cart.totalQuantity ?? cart.itemCount ?? 0);
          if (!cancelled) setCartCount(count);
          return;
        }
        const guestId = getStoredGuestId();
        if (!guestId) {
          if (!cancelled) setCartCount(0);
          return;
        }
        const cart = await getGuestCart(guestId);
        const count = Number(cart.totalQuantity ?? cart.itemCount ?? 0);
        if (!cancelled) setCartCount(count);
      } catch {
        if (!cancelled) setCartCount(0);
      }
    }
    void loadCartCount();
    function onCartChanged() {
      void loadCartCount();
    }
    window.addEventListener("cart:changed", onCartChanged);
    return () => {
      cancelled = true;
      window.removeEventListener("cart:changed", onCartChanged);
    };
  }, [auth.isAuthenticated, isAdminRoute, location.pathname]);

  return (
    <div className={cn("app-shell relative min-h-dvh flex flex-col overflow-hidden bg-background", isAdminRoute && "admin-shell")}>
      <header className={cn("app-header sticky top-0 z-50 border-b backdrop-blur", isAdminRoute && "admin-header")}>
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-x-4 gap-y-2 px-4 py-3 sm:flex-nowrap">
          <Link
            to={isAdminRoute ? "/admin" : "/"}
            className="shrink-0 text-lg font-bold tracking-tight text-white"
          >
            Ecommerce
          </Link>

          {!isAdminRoute ? (
            <form
              className="order-3 w-full sm:order-none sm:w-[360px] md:w-[420px]"
              onSubmit={(e) => {
                e.preventDefault();
                const q = searchQuery.trim();
                if (!q) return;
                navigate(`/products?name=${encodeURIComponent(q)}`);
              }}
            >
              <div className="flex items-center overflow-hidden rounded-md border border-white/10 bg-white">
                <input
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Search products"
                  className="h-9 w-full px-3 text-sm text-foreground outline-none"
                />
                <button
                  type="submit"
                  className="h-9 bg-primary px-4 text-sm font-semibold text-primary-foreground"
                >
                  Search
                </button>
              </div>
            </form>
          ) : null}

          <nav className="w-full flex flex-wrap items-center gap-2 text-sm sm:w-auto sm:ml-auto sm:flex-nowrap sm:justify-end">
            <NavLink to={isAdminRoute ? "/admin" : "/"} end className={navLinkClassName}>
              <span className="inline-flex items-center gap-2 transition-colors">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M3 10.5l9-7 9 7" />
                    <path d="M5 10v10h14V10" />
                  </svg>
                  Home
                </span>
            </NavLink>

            {!isAdminRoute ? (
              <>
                <NavLink to="/categories" className={navLinkClassName}>
                  <span className="inline-flex items-center gap-2 transition-colors">
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
                  <span className="inline-flex items-center gap-2 transition-colors">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 7h12l-1 13H7L6 7z" />
                    <path d="M9 7a3 3 0 0 1 6 0" />
                  </svg>
                  Products
                  </span>
                </NavLink>
              </>
            ) : null}
            {!isAdminRoute ? (
              <NavLink to="/cart" className={navLinkClassName}>
                <span className="relative inline-flex items-center gap-2 transition-colors">
                  <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M6 7h12l-1 13H7L6 7z" />
                    <path d="M9 7a3 3 0 0 1 6 0" />
                  </svg>
                  Cart
                  {cartCount > 0 ? (
                    <span className="absolute -right-3 -top-2 grid h-5 min-w-5 place-items-center rounded-full bg-emerald-500 px-1 text-[10px] font-semibold text-white">
                      {cartCount > 99 ? "99+" : String(cartCount)}
                    </span>
                  ) : null}
                </span>
              </NavLink>
            ) : null}

            {auth.isAuthenticated ? (
              <>
                <div ref={notifRef} className="relative">
                  <button
                    type="button"
                    onClick={() => {
                      setIsMenuOpen(false);
                      setIsNotifOpen((v) => !v);
                    }}
                    className="relative cursor-pointer rounded-md px-3 py-1.5 text-white/80 hover:bg-white/10 hover:text-white"
                    aria-label="Notifications"
                  >
                    <span className="inline-flex items-center gap-2 transition-colors">
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
                    <div className="absolute right-0 mt-2 w-80 overflow-hidden rounded-md border bg-background shadow-md animate-in fade-in zoom-in-95">
                      <div className="flex items-center justify-between gap-2 p-3">
                        <div className="text-sm font-medium">Notifications</div>
                          <button
                            type="button"
                            className="cursor-pointer rounded-md px-2 py-1 text-xs text-muted-foreground hover:bg-muted hover:text-foreground"
                            onClick={() => notifications.markAllRead()}
                          >
                            Mark all as read
                          </button>
                      </div>
                      <div className="max-h-80 overflow-auto border-t">
                        {(notifications.items ?? []).filter((n) => !n.isHidden).slice(0, 6).map((n) => (
                          <div
                            key={String(n.id)}
                            className={[
                              "w-full cursor-pointer px-3 py-3 text-left transition hover:bg-muted",
                              n.isRead ? "bg-muted/40" : "bg-primary/5",
                            ].join(" ")}
                            onClick={() => {
                              const id = Number(n.id ?? 0);
                              if (id) notifications.markRead(id);
                              setIsNotifOpen(false);
                              const route = getNotificationRoute(n);
                              navigate(route || "/notifications");
                            }}
                          >
                            <div className="flex items-start justify-between gap-2">
                              <div className={["text-sm font-medium truncate", n.isRead ? "text-muted-foreground" : "text-foreground"].join(" ")}>{n.title}</div>
                              <span
                                className={[
                                  "shrink-0 rounded-full px-2 py-1 text-[11px] ring-1",
                                  n.isRead ? "bg-muted text-muted-foreground ring-border" : "bg-primary/10 text-primary ring-primary/30",
                                ].join(" ")}
                              >
                                {n.isRead ? "Read" : "Unread"}
                              </span>
                            </div>
                            <div className="mt-1 text-xs text-muted-foreground line-clamp-2">
                              {isInviteNotification(n) && n.isRead
                                ? "This invite has already been handled."
                                : n.message}
                            </div>
                            {isInviteNotification(n) && !n.isRead ? (
                              <div className="mt-2 flex gap-1">
                                <button
                                  type="button"
                                  className="cursor-pointer rounded border px-2 py-1 text-[11px]"
                                  onClick={async (e) => {
                                    e.stopPropagation();
                                    try {
                                      await acceptGroupInvite(Number(n.referenceId));
                                      toast.push({ variant: "success", title: "Invite accepted", message: "You accepted the invite." });
                                      const id = Number(n.id ?? 0);
                                      if (id) notifications.markRead(id);
                                    } catch (err) {
                                      toast.push({ variant: "error", title: "Accept failed", message: getErrorMessage(err, "Please try again.") });
                                    }
                                  }}
                                >
                                  Accept
                                </button>
                                <button
                                  type="button"
                                  className="cursor-pointer rounded border px-2 py-1 text-[11px] text-red-500"
                                  onClick={async (e) => {
                                    e.stopPropagation();
                                    try {
                                      await declineGroupInvite(Number(n.referenceId));
                                      toast.push({ variant: "success", title: "Invite declined", message: "You declined the invite." });
                                      const id = Number(n.id ?? 0);
                                      if (id) notifications.markRead(id);
                                    } catch (err) {
                                      toast.push({ variant: "error", title: "Decline failed", message: getErrorMessage(err, "Please try again.") });
                                    }
                                  }}
                                >
                                  Decline
                                </button>
                              </div>
                            ) : null}
                          </div>
                        ))}
                        {(notifications.items ?? []).filter((n) => !n.isHidden).length === 0 ? (
                          <div className="p-3 text-sm text-muted-foreground">No notifications yet.</div>
                        ) : null}
                      </div>
                      <div className="border-t p-2">
                        <button
                          type="button"
                          className="w-full cursor-pointer rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
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
                <span className="inline-flex items-center gap-2 transition-colors">
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
                  className="rounded-md px-3 py-1.5 text-white/80 hover:bg-white/10 hover:text-white"
                >
                  <span className="inline-flex items-center gap-2 transition-colors">
                    <span className="grid h-6 w-6 place-items-center rounded-full bg-primary text-[10px] font-semibold text-white ring-2 ring-white shadow-sm">
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
                    className="absolute right-0 mt-2 w-64 overflow-hidden rounded-md border bg-background shadow-md animate-in fade-in zoom-in-95"
                    onMouseLeave={() => setIsMenuOpen(false)}
                  >
                    <div className="p-3">
                      <div className="text-sm font-medium truncate">{displayName}</div>
                    </div>
                    <div className="border-t p-2">
                      {availableRoles.length > 1 ? (
                        <div className="mb-2 rounded-md border bg-background/50 p-2">
                          <div className="px-1 pb-2 text-xs font-medium text-muted-foreground">Role</div>
                          <div className="flex items-center gap-2">
                            <select title="Select option" value={roleDraft}
                              onChange={(e) => setRoleDraft(e.target.value)}
                              className="h-9 w-full rounded-md border bg-background px-3 text-sm"
                            >
                              {availableRoles.map((r) => (
                                <option key={r} value={r}>
                                  {r}
                                </option>
                              ))}
                            </select>
                            <button
                              type="button"
                              onClick={() => {
                                const nextRole = roleDraft.trim() || availableRoles[0];
                                setSelectedRole(nextRole);
                                setIsMenuOpen(false);
                                toast.push({ variant: "success", title: "Role switched", message: `Switched to ${nextRole}.` });
                                navigate(defaultRouteForRole(nextRole), { replace: true });
                              }}
                              className="h-9 shrink-0 rounded-md border bg-background px-3 text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                            >
                              Switch
                            </button>
                          </div>
                        </div>
                      ) : null}
                      <button
                        type="button"
                        onClick={() => {
                          setIsMenuOpen(false);
                          navigate(isAdminRoute ? "/admin/profile" : "/me");
                        }}
                        className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                      >
                        Profile
                      </button>
                      {!isAdminRoute ? (
                        <>
                          <button
                            type="button"
                            onClick={() => {
                              setIsMenuOpen(false);
                              navigate("/me/addresses");
                            }}
                            className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          >
                            Addresses
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setIsMenuOpen(false);
                              navigate("/me/wishlist");
                            }}
                            className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          >
                            Wishlist
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setIsMenuOpen(false);
                              navigate("/orders");
                            }}
                            className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          >
                            Orders
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setIsMenuOpen(false);
                              navigate("/me/vouchers");
                            }}
                            className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          >
                            My vouchers
                          </button>
                          <button
                            type="button"
                            onClick={() => {
                              setIsMenuOpen(false);
                              navigate("/me/voucher-uses");
                            }}
                            className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                          >
                            Voucher uses
                          </button>
                        </>
                      ) : null}
                      <button
                        type="button"
                        onClick={() => {
                          auth.logout();
                          setIsMenuOpen(false);
                          toast.push({ variant: "success", title: "Logged out", message: "See you again soon." });
                          navigate(isAdminRoute ? "/admin" : "/", { replace: true });
                        }}
                        className="w-full rounded-md px-3 py-2 text-left text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
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

      {!isAdminRoute ? <ChatbotWidget /> : null}
      <footer className="border-t">
        <div className="mx-auto max-w-6xl px-4 py-6">
          <div className="inline-flex items-center gap-2 rounded-full border border-border bg-white px-3 py-1 text-sm text-muted-foreground">
            <span className="inline-flex h-6 w-6 items-center justify-center rounded-full bg-primary/10 text-primary">
              <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
                <circle cx="12" cy="12" r="8" />
                <path d="M9 10h.01" />
                <path d="M15 10h.01" />
                <path d="M8.5 14.5c1.2 1 2.5 1.5 3.5 1.5s2.3-.5 3.5-1.5" />
              </svg>
            </span>
            Need help? We are here.
          </div>
        </div>
      </footer>
    </div>
  );
}

