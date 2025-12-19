import { Link, NavLink, Outlet } from "react-router-dom";

const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "rounded-full bg-primary/10 px-3 py-1.5 text-foreground ring-1 ring-primary/20"
    : "rounded-full px-3 py-1.5 text-muted-foreground hover:bg-muted hover:text-foreground";

export default function AppLayout() {
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
