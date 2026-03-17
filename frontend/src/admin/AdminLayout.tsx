import { useMemo } from "react";
import { NavLink, Outlet, useLocation, useNavigate } from "react-router-dom";
import { adminNav } from "@/admin/adminNav";
import { cn } from "@/lib/utils";

const linkClass = ({ isActive }: { isActive: boolean }) =>
  cn(
    "group flex items-center gap-2 rounded-md px-3 py-2 text-sm transition",
    isActive
      ? "bg-primary text-primary-foreground shadow-sm"
      : "text-muted-foreground hover:bg-muted hover:text-foreground",
  );

export default function AdminLayout() {
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const groups = Array.from(new Set(adminNav.map((i) => i.group)));

  const activeTo = useMemo(() => {
    const matches = adminNav
      .map((i) => i.to)
      .filter((to) => pathname === to || (to !== "/admin" && pathname.startsWith(`${to}/`)) || (to === "/admin" && pathname.startsWith("/admin")));
    return matches.sort((a, b) => b.length - a.length)[0] ?? "/admin";
  }, [pathname]);

  return (
    <div className="relative min-h-dvh bg-background">
      <div className="mx-auto flex max-w-7xl gap-6 px-4 py-6">
        <aside className="hidden w-64 shrink-0 lg:block">
          <div className="sticky top-24 space-y-4">
            <div className="rounded-md border bg-background p-2 shadow-sm">
              {groups.map((g) => (
                <div key={g} className="py-2">
                  <div className="space-y-1">
                    {adminNav
                      .filter((i) => i.group === g)
                      .map((i) => (
                        <NavLink key={i.to} to={i.to} className={linkClass} end={i.to === "/admin"}>
                          <i.icon className={cn("h-4 w-4", i.to === activeTo ? "text-primary-foreground" : "text-muted-foreground group-hover:text-foreground")} />
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
              <div className="text-2xl font-semibold tracking-tight">Admin dashboard</div>
              <div className="mt-3 lg:hidden">
                <select title="Select option" className="w-full rounded-md border bg-background px-3 py-2 text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                  value={activeTo}
                  onChange={(e) => navigate(e.target.value)}
                >
                  {groups.map((g) => (
                    <optgroup key={g} label={g}>
                      {adminNav
                        .filter((i) => i.group === g)
                        .map((i) => (
                          <option key={i.to} value={i.to}>
                            {i.label}
                          </option>
                        ))}
                    </optgroup>
                  ))}
                </select>
              </div>
            </div>
          </div>

          <Outlet />
        </main>
      </div>
    </div>
  );
}

