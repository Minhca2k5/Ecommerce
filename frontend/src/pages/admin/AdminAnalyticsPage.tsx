import { useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminGet } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";

type FunnelResponse = {
  from: string | [number, number, number];
  to: string | [number, number, number];
  views: number;
  addToCart: number;
  orders: number;
  paymentSuccess: number;
  viewToCartRate: number;
  cartToOrderRate: number;
  orderToPaymentRate: number;
  viewToOrderRate: number;
  previousFrom: string | [number, number, number];
  previousTo: string | [number, number, number];
  previousViews: number;
  previousAddToCart: number;
  previousOrders: number;
  previousPaymentSuccess: number;
  previousViewToOrderRate: number;
  viewsChangeRate: number;
  addToCartChangeRate: number;
  ordersChangeRate: number;
  viewToOrderRateChange: number;
  todayViews: number;
  todayAddToCart: number;
  todayOrders: number;
  todayPaymentSuccess: number;
};

type TopProductResponse = {
  productId: number;
  productName: string;
  views: number;
  addToCart: number;
  orders: number;
  uniqueUsers: number;
  conversionRate: number;
};

function toIsoDate(value: string | [number, number, number] | undefined): string {
  if (!value) return "-";
  if (typeof value === "string") return value;
  const [y, m, d] = value;
  return `${y}-${String(m).padStart(2, "0")}-${String(d).padStart(2, "0")}`;
}

function asPercent(v: number | undefined) {
  const n = Number(v ?? 0);
  return `${(n * 100).toFixed(2)}%`;
}

function trendTextClass(v: number | undefined, hasBaseline = true) {
  if (!hasBaseline) return "text-muted-foreground";
  const n = Number(v ?? 0);
  if (n > 0) return "text-success";
  if (n < 0) return "text-danger";
  return "text-muted-foreground";
}

function asChangePercent(v: number | undefined, hasBaseline: boolean) {
  if (!hasBaseline) return "N/A";
  return asPercent(v);
}

function ymd(date: Date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}-${String(date.getDate()).padStart(2, "0")}`;
}

export default function AdminAnalyticsPage() {
  const toast = useToast();
  const today = useMemo(() => new Date(), []);
  const sevenDaysAgo = useMemo(() => {
    const d = new Date(today);
    d.setDate(d.getDate() - 6);
    return d;
  }, [today]);

  const [from, setFrom] = useState(ymd(sevenDaysAgo));
  const [to, setTo] = useState(ymd(today));
  const [limit, setLimit] = useState("10");
  const [isLoading, setIsLoading] = useState(false);
  const [funnel, setFunnel] = useState<FunnelResponse | null>(null);
  const [topProducts, setTopProducts] = useState<TopProductResponse[]>([]);
  const hasViewsBaseline = Number(funnel?.previousViews ?? 0) > 0;
  const hasAddToCartBaseline = Number(funnel?.previousAddToCart ?? 0) > 0;
  const hasOrdersBaseline = Number(funnel?.previousOrders ?? 0) > 0;
  const hasViewToOrderBaseline = Number(funnel?.previousViewToOrderRate ?? 0) > 0;

  async function load() {
    setIsLoading(true);
    try {
      const query = buildQuery({ from, to });
      const topQuery = buildQuery({ from, to, limit: Number(limit) || 10 });
      const [funnelRes, topRes] = await Promise.all([
        adminGet<FunnelResponse>(`/api/admin/analytics/funnel${query}`),
        adminGet<TopProductResponse[]>(`/api/admin/analytics/top-products${topQuery}`),
      ]);
      setFunnel(funnelRes ?? null);
      setTopProducts(Array.isArray(topRes) ? topRes : []);
    } catch (e) {
      toast.push({
        variant: "error",
        title: "Load failed",
        message: getErrorMessage(e, "Failed to load analytics."),
      });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="space-y-4">
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Analytics</CardTitle>
          </div>
          <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
            {isLoading ? "Loading..." : "Load"}
          </Button>
        </CardHeader>
        <CardContent className="grid gap-3 md:grid-cols-4">
          <Input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="rounded-md" />
          <Input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="rounded-md" />
          <Input type="number" min={1} max={100} value={limit} onChange={(e) => setLimit(e.target.value)} placeholder="Top limit" className="rounded-md" />
          <Button className="rounded-md" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-5">
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Views</CardTitle></CardHeader>
          <CardContent className="text-3xl font-semibold">{(funnel?.views ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Add to Cart</CardTitle></CardHeader>
          <CardContent className="text-3xl font-semibold">{(funnel?.addToCart ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Orders</CardTitle></CardHeader>
          <CardContent className="text-3xl font-semibold">{(funnel?.orders ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Payment Success</CardTitle></CardHeader>
          <CardContent className="text-3xl font-semibold">{(funnel?.paymentSuccess ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Range</CardTitle></CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            {toIsoDate(funnel?.from)} to {toIsoDate(funnel?.to)}
          </CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Previous Range</CardTitle></CardHeader>
          <CardContent className="text-sm text-muted-foreground">
            {toIsoDate(funnel?.previousFrom)} to {toIsoDate(funnel?.previousTo)}
          </CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">View to Cart</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{asPercent(funnel?.viewToCartRate)}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Cart to Order</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{asPercent(funnel?.cartToOrderRate)}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">View to Order</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{asPercent(funnel?.viewToOrderRate)}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Order to Payment</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{asPercent(funnel?.orderToPaymentRate)}</CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Today Views</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{Number(funnel?.todayViews ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Today Add to Cart</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{Number(funnel?.todayAddToCart ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Today Orders</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{Number(funnel?.todayOrders ?? 0).toLocaleString()}</CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Today Payment Success</CardTitle></CardHeader>
          <CardContent className="text-2xl font-semibold">{Number(funnel?.todayPaymentSuccess ?? 0).toLocaleString()}</CardContent>
        </Card>
      </div>

      <div className="grid gap-4 md:grid-cols-4">
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Views vs Previous</CardTitle></CardHeader>
          <CardContent className={`text-2xl font-semibold ${trendTextClass(funnel?.viewsChangeRate, hasViewsBaseline)}`}>
            {asChangePercent(funnel?.viewsChangeRate, hasViewsBaseline)}
          </CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Add to Cart vs Previous</CardTitle></CardHeader>
          <CardContent className={`text-2xl font-semibold ${trendTextClass(funnel?.addToCartChangeRate, hasAddToCartBaseline)}`}>
            {asChangePercent(funnel?.addToCartChangeRate, hasAddToCartBaseline)}
          </CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">Orders vs Previous</CardTitle></CardHeader>
          <CardContent className={`text-2xl font-semibold ${trendTextClass(funnel?.ordersChangeRate, hasOrdersBaseline)}`}>
            {asChangePercent(funnel?.ordersChangeRate, hasOrdersBaseline)}
          </CardContent>
        </Card>
        <Card className="border bg-background shadow-sm">
          <CardHeader><CardTitle className="text-base">View to Order vs Previous</CardTitle></CardHeader>
          <CardContent className={`text-2xl font-semibold ${trendTextClass(funnel?.viewToOrderRateChange, hasViewToOrderBaseline)}`}>
            {asChangePercent(funnel?.viewToOrderRateChange, hasViewToOrderBaseline)}
          </CardContent>
        </Card>
      </div>

      <Card className="border bg-background shadow-sm">
        <CardHeader>
          <CardTitle>Top Products by Conversion</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="table-shell">
            <table className="min-w-[820px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Product</th>
                  <th className="px-4 py-3 text-right font-medium">Views</th>
                  <th className="px-4 py-3 text-right font-medium">Add to Cart</th>
                  <th className="px-4 py-3 text-right font-medium">Orders</th>
                  <th className="px-4 py-3 text-right font-medium">Unique Users</th>
                  <th className="px-4 py-3 text-right font-medium">Conversion</th>
                </tr>
              </thead>
              <tbody>
                {!topProducts.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={6}>
                      No analytics rows found for selected range.
                    </td>
                  </tr>
                ) : (
                  topProducts.map((p) => (
                    <tr key={p.productId} className="border-t">
                      <td className="px-4 py-3">
                        <div className="font-medium">{p.productName || `Product #${p.productId}`}</div>
                        <div className="text-sm text-muted-foreground">ID: {p.productId}</div>
                        {Number(p.orders ?? 0) > Number(p.views ?? 0) ? (
                          <div className="mt-1 text-sm text-amber-600">Attribution mismatch: orders {">"} views</div>
                        ) : null}
                      </td>
                      <td className="px-4 py-3 text-right">{Number(p.views ?? 0).toLocaleString()}</td>
                      <td className="px-4 py-3 text-right">{Number(p.addToCart ?? 0).toLocaleString()}</td>
                      <td className="px-4 py-3 text-right">{Number(p.orders ?? 0).toLocaleString()}</td>
                      <td className="px-4 py-3 text-right">{Number(p.uniqueUsers ?? 0).toLocaleString()}</td>
                      <td className="px-4 py-3 text-right font-semibold">{asPercent(p.conversionRate)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

