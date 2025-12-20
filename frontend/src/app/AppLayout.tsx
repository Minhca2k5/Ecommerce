import { useEffect, useMemo, useRef, useState } from "react";
import { Link, NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";

const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "rounded-full bg-primary/10 px-3 py-1.5 text-foreground ring-1 ring-primary/20"
    : "rounded-full px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground";

export default function AppLayout() {
  const auth = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement | null>(null);

  const displayName = useMemo(() => auth.user?.fullName || auth.user?.username || auth.user?.email || "Account", [auth.user?.email, auth.user?.fullName, auth.user?.username]);

  useEffect(() => {
    if (!isMenuOpen) return;
    function onPointerDown(e: PointerEvent) {
      const target = e.target as Node | null;
      if (!target) return;
      if (menuRef.current?.contains(target)) return;
      setIsMenuOpen(false);
    }
    window.addEventListener("pointerdown", onPointerDown);
    return () => window.removeEventListener("pointerdown", onPointerDown);
  }, [isMenuOpen]);

  return (
    <div className="relative min-h-dvh flex flex-col overflow-hidden bg-background">
      <div className="pointer-events-none absolute inset-0 animated-aurora opacity-70" />
      <header className="sticky top-0 z-50 border-b bg-background/75 backdrop-blur supports-[backdrop-filter]:bg-background/50">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link
            to="/"
            className="font-semibold tracking-tight text-transparent bg-clip-text bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500"
          >
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
        <div className="mx-auto max-w-6xl px-4 py-6 text-sm text-muted-foreground">
          © {new Date().getFullYear()} Ecommerce
        </div>
      </footer>
    </div>
  );
}
