import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { listMyOrders, type OrderResponse } from "@/lib/orderApi";
import { createAuthedEventSource } from "@/lib/sse";

function statusBadge(status?: string) {
  const normalized = (status || "PENDING").toUpperCase();
  const cls =
    normalized === "PAID" || normalized === "COMPLETED"
      ? "bg-emerald-500/10 text-emerald-700 ring-emerald-500/20"
      : normalized === "CANCELLED" || normalized === "CANCELED"
        ? "bg-rose-500/10 text-rose-700 ring-rose-500/20"
        : "bg-primary/10 text-foreground ring-primary/20";
  return <span className={`rounded-full px-2 py-1 text-xs ring-1 ${cls}`}>{normalized}</span>;
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
    const load = () => listMyOrders()
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
      listMyOrders()
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
        title="Couldn't load orders"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-xl bg-primary text-primary-foreground">
            Retry
          </Button>
        }
      />
    );
  }

  if (!sorted.length) {
    return (
      <EmptyState
        title="No orders yet"
        description="Place your first order from the cart."
        action={
          <Button asChild className="h-10 rounded-xl bg-primary text-primary-foreground">
            <Link to="/products">Browse products</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="text-2xl font-semibold">Orders</div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="h-10 rounded-xl bg-background">
              <Link to="/cart">Cart</Link>
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-4 lg:grid-cols-2">
        {sorted.map((order) => (
          <Card key={String(order.id)} className="pressable overflow-hidden">
            <CardHeader className="relative flex flex-row items-start justify-between gap-3">
              <div>
                <CardTitle>Order</CardTitle>
                <div className="mt-1 text-xs text-muted-foreground">
                  {`Subtotal ${formatCurrency(Number(order.subtotalAmount ?? 0), order.currency || "VND")} • Tax ${formatCurrency(Number(order.taxAmount ?? 0), order.currency || "VND")}`}
                </div>
                <div className="mt-1 text-sm text-muted-foreground">Total • {formatCurrency(Number(order.totalAmount ?? 0), order.currency || "VND")}</div>
              </div>
              {statusBadge(order.status)}
            </CardHeader>
            <CardContent className="relative flex items-center justify-between">
              <Button asChild className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90">
                <Link to={`/orders/${order.id}`}>View</Link>
              </Button>
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}
