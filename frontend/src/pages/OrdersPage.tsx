import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { apiJson } from "@/lib/http";
import { listMyOrders, type OrderResponse } from "@/lib/orderApi";
import { createAuthedEventSource } from "@/lib/sse";

function dedupeOrders(rows: OrderResponse[]) {
  const map = new Map<number, OrderResponse>();
  for (const row of rows) {
    const id = Number(row.id ?? 0);
    if (!id) continue;
    if (!map.has(id)) map.set(id, row);
  }
  return Array.from(map.values());
}

function statusBadge(status?: string) {
  const normalized = (status || "PENDING").toUpperCase();
  const labelMap: Record<string, string> = {
    PENDING: "Pending",
    PAID: "Paid",
    COMPLETED: "Completed",
    CANCELLED: "Cancelled",
    CANCELED: "Cancelled",
    PROCESSING: "Processing",
    SHIPPED: "Shipped",
    DELIVERED: "Delivered",
    FAILED: "Failed",
  };
  const cls =
    normalized === "PAID" || normalized === "COMPLETED"
      ? "bg-emerald-500/10 text-emerald-700 ring-emerald-500/20"
      : normalized === "CANCELLED" || normalized === "CANCELED"
        ? "bg-rose-500/10 text-rose-700 ring-rose-500/20"
        : "bg-primary/10 text-foreground ring-primary/20";
  return <span className={`rounded-full px-2 py-1 text-xs ring-1 ${cls}`}>{labelMap[normalized] ?? "Pending"}</span>;
}

function orderPreview(order: OrderResponse) {
  const items = Array.isArray(order.items) ? order.items : [];
  const names = items
    .map((it) => String(it?.productName ?? "").trim())
    .filter(Boolean);
  const totalQty = Math.max(0, items.reduce((sum, it) => sum + Number(it?.quantity ?? 1), 0));
  if (!names.length) {
    const fallbackCount = Number(order.itemCount ?? 0);
    const qty = totalQty || fallbackCount;
    return {
      title: "Order items",
      subtitle: qty > 0 ? `Qty ${qty}` : "Open details to view product list.",
    };
  }

  const firstQty = Number(items[0]?.quantity ?? 1);
  const title = `${names[0]}${firstQty > 0 ? ` · Qty ${firstQty}` : ""}`;
  const extra = names.length - 1;
  const subtitle = extra > 0
    ? `+${extra} more item${extra > 1 ? "s" : ""} • Total qty ${Math.max(totalQty, firstQty)}`
    : `Total qty ${Math.max(totalQty, firstQty)}`;
  return { title, subtitle };
}

async function loadOrdersWithItems() {
  const rows = dedupeOrders(await listMyOrders());
  const enriched = await Promise.all(
    rows.map(async (order) => {
      const orderId = Number(order.id ?? 0);
      const existingItems = Array.isArray(order.items) ? order.items : [];
      const hasNamedItems = existingItems.some((it) => String(it?.productName ?? "").trim().length > 0);
      if (!orderId || hasNamedItems) return order;

      const fetchedItems = await apiJson<any[]>(`/api/users/me/orders/${orderId}/items/all`, {
        method: "GET",
        auth: true,
      }).catch(() => []);

      if (!Array.isArray(fetchedItems) || !fetchedItems.length) return order;
      const normalizedItems = fetchedItems.map((it: any) => ({
        id: it?.id,
        orderId: it?.orderId,
        productId: it?.productId,
        productName: String(it?.productName ?? it?.productNameSnapshot ?? it?.name ?? "").trim() || undefined,
        quantity: Number(it?.quantity ?? 1),
        lineTotal: Number(it?.lineTotal ?? 0),
      }));

      return {
        ...order,
        items: normalizedItems,
        itemCount: Number(order.itemCount ?? normalizedItems.length),
      } as OrderResponse;
    })
  );

  return enriched;
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const sorted = useMemo(() => {
    const next = [...orders];
    next.sort((a, b) => Number(b.id ?? 0) - Number(a.id ?? 0));
    return next;
  }, [orders]);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);
    const load = () => loadOrdersWithItems()
      .then((data) => {
        if (!alive) return;
        setOrders(data);
      })
      .catch((e) => {
        if (!alive) return;
        setError(getErrorMessage(e, "Failed to load orders."));
      })
      .finally(() => alive && setIsLoading(false));
    load();
    return () => {
      alive = false;
    };
  }, []);

  useEffect(() => {
    const es = createAuthedEventSource("/api/users/me/realtime/orders");
    const refresh = () => {
      loadOrdersWithItems()
        .then((data) => setOrders(data))
        .catch(() => {
          // ignore
        });
    };
    es.addEventListener("order-status", refresh);
    es.addEventListener("payment-status", refresh);
    es.onerror = () => {
      // ignore
    };
    return () => es.close();
  }, []);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (error) {
    return (
      <EmptyState
        title="Could not load orders"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-md bg-primary text-primary-foreground">
            Try again
          </Button>
        }
      />
    );
  }

  if (!sorted.length) {
    return (
      <EmptyState
        title="No orders yet"
        description="Your orders will show up here after checkout."
        action={
          <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
            <Link to="/products">Browse all products</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-8">
      <section className="page-hero">
        <div className="hero-orb hero-orb--a" />
        <div className="hero-orb hero-orb--b" />
        <div className="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="max-w-2xl space-y-2">
            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Orders</div>
            <div className="text-3xl font-semibold tracking-tight">Track every order</div>
            <p className="text-sm text-muted-foreground">
              See status updates and open details in one place.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="h-10 rounded-md bg-white">
              <Link to="/cart">Go to cart</Link>
            </Button>
            <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
              <Link to="/products">Browse all products</Link>
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-4 lg:grid-cols-2">
        {sorted.map((order) => {
          const preview = orderPreview(order);
          return (
            <Card key={String(order.id)} className="pressable relative overflow-hidden bg-white">
              <CardHeader className="relative flex flex-row items-start justify-between gap-3">
                <div>
                  <CardTitle className="line-clamp-1">{preview.title}</CardTitle>
                  <div className="mt-1 text-xs text-muted-foreground">{preview.subtitle}</div>
                  <div className="mt-1 text-xs text-muted-foreground">
                    {`Subtotal ${formatCurrency(Number(order.subtotalAmount ?? 0), order.currency || "VND")} | Tax ${formatCurrency(Number(order.taxAmount ?? 0), order.currency || "VND")}`}
                  </div>
                  <div className="mt-1 text-sm text-muted-foreground">Total - {formatCurrency(Number(order.totalAmount ?? 0), order.currency || "VND")}</div>
                </div>
                {statusBadge(order.status)}
              </CardHeader>
              <CardContent className="relative flex items-center justify-between">
                <Button asChild className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90">
                  <Link to={`/orders/${order.id}`}>View details</Link>
                </Button>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}

