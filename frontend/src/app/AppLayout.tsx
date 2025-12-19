import { Link, NavLink, Outlet } from "react-router-dom";

const navLinkClassName = ({ isActive }: { isActive: boolean }) =>
  isActive
    ? "text-foreground"
    : "text-muted-foreground hover:text-foreground";

export default function AppLayout() {
  return (
    <div className="min-h-dvh flex flex-col">
      <header className="sticky top-0 z-50 border-b bg-background/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3">
          <Link to="/" className="font-semibold tracking-tight">
            Ecommerce
          </Link>

          <nav className="flex items-center gap-4 text-sm">
            <NavLink to="/" end className={navLinkClassName}>
              Home
            </NavLink>
            <NavLink to="/products" className={navLinkClassName}>
              Products
            </NavLink>
            <NavLink to="/login" className={navLinkClassName}>
              Login
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

