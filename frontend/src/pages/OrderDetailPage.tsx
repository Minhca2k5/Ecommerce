import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import SafeImage from "@/components/SafeImage";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { getNumber } from "@/lib/safe";
import { getMyOrder, type OrderResponse } from "@/lib/orderApi";
import { createPayment, listPayments, type PaymentResponse } from "@/lib/paymentApi";
import { useNotifications } from "@/app/NotificationProvider";

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

export default function OrderDetailPage() {
  const toast = useToast();
  const navigate = useNavigate();
  const params = useParams();
  const notifications = useNotifications();

  const orderId = useMemo(() => Number(params.orderId ?? "0"), [params.orderId]);
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [method, setMethod] = useState("COD");
  const [providerTxnId, setProviderTxnId] = useState("");
  const [isPaying, setIsPaying] = useState(false);

  const currency = order?.currency || "VND";
  const voucherId = useMemo(() => getNumber(order, "voucherId"), [order]);
  const discountAmount = useMemo(() => getNumber(order, "discountAmount"), [order]);

  useEffect(() => {
    if (!orderId) return;
    let alive = true;
    setIsLoading(true);
    setError(null);
    Promise.all([getMyOrder(orderId), listPayments(orderId).catch(() => [])])
      .then(([o, p]) => {
        if (!alive) return;
        setOrder(o);
        setPayments(p);
      })
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load order.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [orderId]);

  async function onCreatePayment() {
    if (!orderId) return;
    setIsPaying(true);
    try {
      await createPayment(orderId, {
        orderId,
        method,
        status: "INITIATED",
        ...(providerTxnId.trim() ? { providerTxnId: providerTxnId.trim() } : {}),
        ...(voucherId !== undefined ? { voucherId } : {}),
        ...(discountAmount !== undefined ? { discountAmount } : {}),
      });
      toast.push({ variant: "success", title: "Payment created", message: "Payment created for this order." });
      notifications.push({
        type: "PAYMENT",
        title: `Payment created for order #${orderId}`,
        message: `Method: ${method}. You can track status in this order.`,
        referenceId: orderId,
        referenceType: "ORDER",
      });
      setProviderTxnId("");
      const updated = await listPayments(orderId).catch(() => []);
      setPayments(updated);
    } catch (e) {
      toast.push({ variant: "error", title: "Payment failed", message: getErrorMessage(e, "Failed to create payment.") });
    } finally {
      setIsPaying(false);
    }
  }

  if (!orderId) return <EmptyState title="Invalid order" description="Missing orderId." />;
  if (isLoading) return <div className="space-y-4"><LoadingCard /><LoadingCard /></div>;
  if (error || !order) {
    return (
      <EmptyState
        title="Couldn't load order"
        description={error || "Order not found."}
        action={
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/orders">Back to orders</Link>
          </Button>
        }
      />
    );
  }

  const items = (order.items ?? []) as any[];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Order</div>
          <div className="text-3xl font-semibold tracking-tight">Order #{order.id}</div>
          <div className="mt-1 text-sm text-muted-foreground">
            {items.length} items • {formatCurrency(Number(order.totalAmount ?? 0), currency)} • {statusBadge(order.status)}
          </div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="rounded-xl" onClick={() => navigate("/orders")}>
            Back
          </Button>
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
            <Link to="/products">Shop more</Link>
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="shine">
          <CardHeader>
            <CardTitle className="text-base">Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {items.length ? (
              items.map((it, idx) => (
                <div key={String(it?.id ?? idx)} className="flex gap-3 rounded-2xl border bg-background/60 p-3 backdrop-blur">
                  <div className="h-16 w-16 overflow-hidden rounded-xl border bg-muted">
                    <SafeImage
                      src={it?.url ?? ""}
                      alt={it?.productName ?? "Product"}
                      fallbackKey={String(it?.id ?? it?.productId ?? idx)}
                      className="h-full w-full object-cover"
                    />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-medium">{it?.productName ?? "Product"}</div>
                    <div className="mt-1 text-xs text-muted-foreground">Qty: {it?.quantity ?? 1}</div>
                    <div className="mt-2">
                      <Button asChild variant="outline" className="rounded-xl">
                        <Link to={`/products/${it?.productId ?? ""}`}>View product</Link>
                      </Button>
                    </div>
                  </div>
                  <div className="text-right text-sm font-semibold">{formatCurrency(Number(it?.lineTotal ?? 0), currency)}</div>
                </div>
              ))
            ) : (
              <div className="text-sm text-muted-foreground">No items.</div>
            )}
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Payment</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Method</div>
                <div className="flex flex-wrap gap-2">
                  {["COD", "CARD", "EWALLET"].map((m) => (
                    <button
                      key={m}
                      type="button"
                      onClick={() => setMethod(m)}
                      className={[
                        "pressable rounded-full border px-3 py-1 text-xs shadow-sm transition hover:-translate-y-0.5",
                        method === m ? "border-primary bg-primary text-primary-foreground" : "bg-background hover:bg-muted",
                      ].join(" ")}
                    >
                      {m}
                    </button>
                  ))}
                </div>
              </div>
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Provider txn id (optional)</div>
                <Input className="rounded-xl" value={providerTxnId} onChange={(e) => setProviderTxnId(e.target.value)} placeholder="e.g. VNPAY-..." />
              </div>
              <Button disabled={isPaying} onClick={onCreatePayment} className="w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
                {isPaying ? "Creating..." : "Create payment"}
              </Button>
              <div className="text-xs text-muted-foreground">
                This creates a payment record via backend; gateway integration can be added later.
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Payments</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {payments.length ? (
                payments.map((p) => (
                  <div key={String(p.id)} className="flex items-center justify-between rounded-xl border bg-background/60 px-3 py-2">
                    <div className="min-w-0">
                      <div className="text-sm font-medium">{p.method || "Payment"} • {formatCurrency(Number(p.amount ?? 0), p.orderCurrency || currency)}</div>
                      <div className="text-xs text-muted-foreground truncate">{p.providerTxnId || "-"}</div>
                    </div>
                    <span className="rounded-full bg-muted px-2 py-1 text-xs text-muted-foreground ring-1 ring-border">{(p.status || "INITIATED").toUpperCase()}</span>
                  </div>
                ))
              ) : (
                <div className="text-sm text-muted-foreground">No payments yet.</div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
