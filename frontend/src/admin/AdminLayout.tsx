import { NavLink, Outlet, useLocation } from "react-router-dom";
import { adminNav } from "@/admin/adminNav";
import { cn } from "@/lib/utils";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    "group flex items-center gap-2 rounded-xl px-3 py-2 text-sm transition",
    isActive
      ? "bg-gradient-to-r from-primary/15 via-fuchsia-500/10 to-emerald-500/10 text-foreground ring-1 ring-primary/15"
      : "text-muted-foreground hover:bg-muted hover:text-foreground",
  );

function Breadcrumbs() {
  const { pathname } = useLocation();
  const parts = pathname.split("/").filter(Boolean);
  const crumbs = parts.slice(0, 3); // admin + section + maybe detail
  return (
    <div className="text-xs text-muted-foreground">
      {crumbs.map((c, idx) => (
        <span key={`${c}-${idx}`}>
          {idx > 0 ? <span className="mx-1">/</span> : null}
          <span className="capitalize">{c.replace(/-/g, " ")}</span>
        </span>
      ))}
    </div>
  );
}

export default function AdminLayout() {
  const groups = Array.from(new Set(adminNav.map((i) => i.group)));

  return (
    <div className="relative min-h-dvh bg-background">
      <div className="pointer-events-none absolute inset-0 animated-aurora opacity-55" />
      <div className="mx-auto flex max-w-7xl gap-6 px-4 py-6">
        <aside className="hidden w-64 shrink-0 lg:block">
          <div className="sticky top-24 space-y-4">
            <div className="rounded-2xl border bg-background/75 p-4 shadow-sm backdrop-blur">
              <div className="text-sm font-semibold">Admin</div>
              <div className="mt-1 text-xs text-muted-foreground">Manage catalog, sales, users, and system.</div>
            </div>

            <div className="rounded-2xl border bg-background/75 p-2 shadow-sm backdrop-blur">
              {groups.map((g) => (
                <div key={g} className="py-2">
                  <div className="px-3 pb-1 text-[11px] font-medium uppercase tracking-wide text-muted-foreground/80">{g}</div>
                  <div className="space-y-1">
                    {adminNav
                      .filter((i) => i.group === g)
                      .map((i) => (
                        <NavLink key={i.to} to={i.to} className={linkClass} end={i.to === "/admin"}>
                          <i.icon className="h-4 w-4 text-muted-foreground group-hover:text-foreground" />
                          <span>{i.label}</span>
                        </NavLink>
                      ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </aside>

        <main className="min-w-0 flex-1">
          <div className="mb-4 flex items-end justify-between gap-3">
            <div className="min-w-0">
              <Breadcrumbs />
              <div className="text-2xl font-semibold tracking-tight">Admin dashboard</div>
            </div>
            <div className="hidden lg:block text-xs text-muted-foreground">ROLE_ADMIN protected</div>
          </div>

          <Outlet />
        </main>
      </div>
    </div>
  );
}

