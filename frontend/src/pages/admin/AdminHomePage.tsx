import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { adminNav } from "@/admin/adminNav";
import { adminGet, type PageResponse } from "@/lib/adminApi";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { asArray, getString } from "@/lib/safe";

type Totals = {
  products: number | null;
  categories: number | null;
  orders: number | null;
  users: number | null;
  reviews: number | null;
  vouchers: number | null;
  payments: number | null;
};

function StatCard({ label, value, to }: { label: string; value: number | null; to: string }) {
  return (
    <Link to={to} className="block">
      <Card className="pressable border bg-background shadow-sm transition hover:shadow-md">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm font-medium text-muted-foreground">{label}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-2xl font-semibold">{typeof value === "number" ? value.toLocaleString() : "-"}</div>
        </CardContent>
      </Card>
    </Link>
  );
}

function MiniBarChart({ title, data }: { title: string; data: Array<{ label: string; value: number }> }) {
  const max = Math.max(1, ...data.map((d) => d.value));
  return (
    <Card className="border bg-background shadow-sm">
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-2">
        {data.length === 0 ? (
          <div className="text-sm text-muted-foreground">No data yet.</div>
        ) : (
          data.map((d) => (
            <div key={d.label} className="space-y-1">
              <div className="flex items-center justify-between text-sm text-muted-foreground">
                <span className="font-medium text-foreground">{d.label}</span>
                <span>{d.value.toLocaleString()}</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full border bg-background">
                <div className="h-full rounded-full bg-primary/70" style={{ width: `${Math.round((d.value / max) * 100)}%` }} />
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
}

function PieChart({ title, data }: { title: string; data: Array<{ label: string; value: number }> }) {
  const total = data.reduce((s, d) => s + d.value, 0) || 1;
  const size = 140;
  const r = 54;
  const cx = size / 2;
  const cy = size / 2;
  const colors = ["#22c55e", "#3b82f6", "#a855f7", "#f97316", "#ef4444", "#14b8a6", "#eab308"];

  let acc = 0;
  const slices = data.map((d, idx) => {
    const start = (acc / total) * Math.PI * 2;
    acc += d.value;
    const end = (acc / total) * Math.PI * 2;

    const x1 = cx + r * Math.cos(start - Math.PI / 2);
    const y1 = cy + r * Math.sin(start - Math.PI / 2);
    const x2 = cx + r * Math.cos(end - Math.PI / 2);
    const y2 = cy + r * Math.sin(end - Math.PI / 2);
    const largeArc = end - start > Math.PI ? 1 : 0;
    const path = `M ${cx} ${cy} L ${x1} ${y1} A ${r} ${r} 0 ${largeArc} 1 ${x2} ${y2} Z`;

    return { label: d.label, value: d.value, color: colors[idx % colors.length], path };
  });

  return (
    <Card className="border bg-background shadow-sm">
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-4 sm:grid-cols-[160px_1fr] sm:items-center">
        <div className="flex items-center justify-center">
          <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="overflow-visible">
            {slices.map((s) => (
              <path key={s.label} d={s.path} fill={s.color} opacity={0.9} />
            ))}
            <circle cx={cx} cy={cy} r={28} fill="rgba(0,0,0,0)" />
          </svg>
        </div>
        <div className="grid gap-2">
          {data.length === 0 ? (
            <div className="text-sm text-muted-foreground">No data yet.</div>
          ) : (
            data.map((d, idx) => (
              <div key={d.label} className="flex items-center justify-between gap-3 text-sm">
                <div className="flex items-center gap-2">
                  <span className="h-2 w-2 rounded-full" style={{ background: colors[idx % colors.length] }} />
                  <span className="text-muted-foreground">{d.label}</span>
                </div>
                <span className="font-medium text-foreground">{d.value.toLocaleString()}</span>
              </div>
            ))
          )}
        </div>
      </CardContent>
    </Card>
  );
}

function LineChart({ title, data }: { title: string; data: Array<{ label: string; value: number }> }) {
  const w = 520;
  const h = 160;
  const pad = 18;
  const max = Math.max(1, ...data.map((d) => d.value));
  const step = data.length > 1 ? (w - pad * 2) / (data.length - 1) : 0;

  const points = data
    .map((d, i) => {
      const x = pad + i * step;
      const y = h - pad - (d.value / max) * (h - pad * 2);
      return [x, y] as const;
    })
    .map((p) => p.join(","))
    .join(" ");

  return (
    <Card className="border bg-background shadow-sm">
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {data.length === 0 ? (
          <div className="text-sm text-muted-foreground">No data yet.</div>
        ) : (
          <>
            <svg viewBox={`0 0 ${w} ${h}`} className="w-full">
              <polyline points={points} fill="none" stroke="url(#line)" strokeWidth="3" strokeLinejoin="round" />
              <defs>
                <linearGradient id="line" x1="0" y1="0" x2="1" y2="0">
                  <stop offset="0%" stopColor="#3b82f6" />
                  <stop offset="50%" stopColor="#a855f7" />
                  <stop offset="100%" stopColor="#22c55e" />
                </linearGradient>
              </defs>
            </svg>
            <div className="flex items-center justify-between text-sm text-muted-foreground">
              <span>{data[0]?.label}</span>
              <span>{data[data.length - 1]?.label}</span>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
}

function BarChart({ title, data }: { title: string; data: Array<{ label: string; value: number }> }) {
  const max = Math.max(1, ...data.map((d) => d.value));
  return (
    <Card className="border bg-background shadow-sm">
      <CardHeader>
        <CardTitle className="text-base">{title}</CardTitle>
      </CardHeader>
      <CardContent className="grid gap-3">
        {data.length === 0 ? (
          <div className="text-sm text-muted-foreground">No data yet.</div>
        ) : (
          data.map((d) => (
            <div key={d.label} className="grid gap-1">
              <div className="flex items-center justify-between text-sm text-muted-foreground">
                <span className="font-medium text-foreground">{d.label}</span>
                <span>{d.value.toLocaleString()}</span>
              </div>
              <div className="h-2 overflow-hidden rounded-full border bg-background">
                <div className="h-full rounded-full bg-primary/60" style={{ width: `${Math.round((d.value / max) * 100)}%` }} />
              </div>
            </div>
          ))
        )}
      </CardContent>
    </Card>
  );
}

export default function AdminHomePage() {
  const groups = Array.from(new Set(adminNav.filter((i) => i.to !== "/admin").map((i) => i.group)));

  const [totals, setTotals] = useState<Totals>({
    products: null,
    categories: null,
    orders: null,
    users: null,
    reviews: null,
    vouchers: null,
    payments: null,
  });
  const [recentOrderStatus, setRecentOrderStatus] = useState<Record<string, number>>({});
  const [ordersByDay, setOrdersByDay] = useState<Array<{ label: string; value: number }>>([]);

  useEffect(() => {
    let alive = true;

    async function loadTotals() {
      const endpoints: Array<[keyof Totals, string]> = [
        ["products", "/api/admin/products?page=0&size=1"],
        ["categories", "/api/admin/categories?page=0&size=1"],
        ["orders", "/api/admin/orders?page=0&size=1"],
        ["users", "/api/admin/users?page=0&size=1"],
        ["reviews", "/api/admin/reviews?page=0&size=1"],
        ["vouchers", "/api/admin/vouchers?page=0&size=1"],
        ["payments", "/api/admin/payments?page=0&size=1"],
      ];

      const results = await Promise.allSettled(endpoints.map(([, url]) => adminGet<PageResponse<unknown>>(url)));
      if (!alive) return;

      const next: Totals = { ...totals };
      for (let i = 0; i < endpoints.length; i++) {
        const key = endpoints[i][0];
        const res = results[i];
        next[key] = res.status === "fulfilled" ? Number(res.value?.totalElements ?? null) : null;
      }
      setTotals(next);
    }

    async function loadRecentOrdersStatus() {
      const res = await adminGet<PageResponse<unknown>>(`/api/admin/orders?page=0&size=50&sort=id,desc`);
      if (!alive) return;
      const list = asArray(res?.content) as unknown[];
      const counts: Record<string, number> = {};
      const byDay: Record<string, number> = {};
      for (const o of list) {
        const status = (getString(o, "status") || "UNKNOWN").toUpperCase();
        counts[status] = (counts[status] ?? 0) + 1;

        const createdAt = getString(o, "createdAt");
        if (createdAt) {
          const dt = new Date(createdAt);
          if (!Number.isNaN(dt.getTime())) {
            const key = `${dt.getFullYear()}-${String(dt.getMonth() + 1).padStart(2, "0")}-${String(dt.getDate()).padStart(2, "0")}`;
            byDay[key] = (byDay[key] ?? 0) + 1;
          }
        }
      }
      setRecentOrderStatus(counts);

      const days: Array<{ label: string; value: number }> = [];
      const now = new Date();
      for (let i = 13; i >= 0; i--) {
        const d = new Date(now);
        d.setDate(now.getDate() - i);
        const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
        days.push({ label: key.slice(5), value: byDay[key] ?? 0 });
      }
      setOrdersByDay(days);
    }

    void loadTotals();
    void loadRecentOrdersStatus().catch(() => setRecentOrderStatus({}));

    return () => {
      alive = false;
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const statusChartData = useMemo(() => {
    const entries = Object.entries(recentOrderStatus);
    entries.sort((a, b) => b[1] - a[1]);
    return entries.slice(0, 7).map(([label, value]) => ({ label, value }));
  }, [recentOrderStatus]);

  const totalsBarData = useMemo(() => {
    const entries: Array<{ label: string; value: number }> = [];
    if (typeof totals.products === "number") entries.push({ label: "Products", value: totals.products });
    if (typeof totals.categories === "number") entries.push({ label: "Categories", value: totals.categories });
    if (typeof totals.orders === "number") entries.push({ label: "Orders", value: totals.orders });
    if (typeof totals.users === "number") entries.push({ label: "Users", value: totals.users });
    if (typeof totals.reviews === "number") entries.push({ label: "Reviews", value: totals.reviews });
    if (typeof totals.vouchers === "number") entries.push({ label: "Vouchers", value: totals.vouchers });
    if (typeof totals.payments === "number") entries.push({ label: "Payments", value: totals.payments });
    return entries;
  }, [totals]);

  return (
    <div className="grid gap-4">
      <Card className="overflow-hidden border bg-background shadow-sm">
        <CardHeader>
          <CardTitle>Overview</CardTitle>
        </CardHeader>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
        <StatCard label="Products" value={totals.products} to="/admin/products" />
        <StatCard label="Categories" value={totals.categories} to="/admin/categories" />
        <StatCard label="Orders" value={totals.orders} to="/admin/orders" />
        <StatCard label="Users" value={totals.users} to="/admin/users" />
        <StatCard label="Reviews" value={totals.reviews} to="/admin/reviews" />
        <StatCard label="Vouchers" value={totals.vouchers} to="/admin/vouchers" />
        <StatCard label="Payments" value={totals.payments} to="/admin/payments" />
</div>

      <div className="grid gap-4 lg:grid-cols-2">
        <PieChart title="Orders by status (last 50)" data={statusChartData} />
        <LineChart title="Orders trend (last 14 days)" data={ordersByDay} />
        <BarChart title="Entities overview" data={totalsBarData} />
        <MiniBarChart title="Top status counts" data={statusChartData} />
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        {groups.map((g) => (
          <Card key={g} className="border bg-background shadow-sm">
            <CardHeader>
              <CardTitle className="text-base">{g}</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-2">
              {adminNav
                .filter((i) => i.group === g && i.to !== "/admin")
                .map((i) => (
                  <Link
                    key={i.to}
                    to={i.to}
                    className="pressable inline-flex items-center gap-2 rounded-xl border bg-background px-3 py-2 text-sm text-muted-foreground shadow-sm transition hover:bg-muted hover:text-foreground hover:shadow-md"
                  >
                    <i.icon className="h-4 w-4" />
                    {i.label}
                  </Link>
                ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
