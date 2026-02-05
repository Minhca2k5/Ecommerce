import { useEffect, useMemo, useState } from "react";
import { Link, useSearchParams, useParams } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { createGuestMomoPayment } from "@/lib/momoApi";
import { getGuestOrder, type OrderResponse } from "@/lib/orderApi";
import { listGuestPayments, type PaymentResponse } from "@/lib/paymentApi";

export default function GuestOrderPage() {
  const toast = useToast();
  const { orderId: orderIdRaw } = useParams();
  const [searchParams] = useSearchParams();

  const orderId = useMemo(() => Number(orderIdRaw ?? "0"), [orderIdRaw]);
  const accessToken = useMemo(() => searchParams.get("token") || searchParams.get("accessToken") || "", [searchParams]);

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [payments, setPayments] = useState<PaymentResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isMomoLoading, setIsMomoLoading] = useState(false);

  useEffect(() => {
    if (!orderId || !accessToken) {
      setIsLoading(false);
      setError("Missing guest order access token.");
      return;
    }
    let alive = true;
    setIsLoading(true);
    setError(null);
    Promise.all([getGuestOrder(orderId, accessToken), listGuestPayments(orderId, accessToken).catch(() => [])])
      .then(([o, p]) => {
        if (!alive) return;
        setOrder(o);
        setPayments(p);
      })
      .catch((e) => alive && setError(getErrorMessage(e, "Cannot load guest order.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [accessToken, orderId]);

  async function onPayWithMomo() {
    if (!orderId || !accessToken) return;
    setIsMomoLoading(true);
    try {
      const idempotencyKey =
        typeof crypto !== "undefined" && "randomUUID" in crypto ? crypto.randomUUID() : `guest_momo_${Date.now()}`;
      const res = await createGuestMomoPayment(orderId, accessToken, idempotencyKey);
      const payUrl = res.payUrl || res.deeplink || res.qrCodeUrl;
      if (!payUrl) {
        throw new Error(res.message || "No payment URL returned.");
      }
      window.open(payUrl, "_blank");
      const updated = await listGuestPayments(orderId, accessToken).catch(() => []);
      setPayments(updated);
      toast.push({ variant: "success", title: "MoMo created", message: "Complete payment in the new tab." });
    } catch (e) {
      toast.push({ variant: "error", title: "MoMo failed", message: getErrorMessage(e, "Could not start MoMo payment.") });
    } finally {
      setIsMomoLoading(false);
    }
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (error || !order) {
    return (
      <EmptyState
        title="Guest order unavailable"
        description={error || "Order not found."}
        action={
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/products">Back to products</Link>
          </Button>
        }
      />
    );
  }

  const currency = order.currency || "VND";

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative">
          <div className="text-sm text-muted-foreground">Guest order</div>
          <div className="text-3xl font-semibold tracking-tight">Order #{order.id}</div>
          <div className="mt-1 text-sm text-muted-foreground">
            Status: {(order.status || "PENDING").toUpperCase()} - Total: {formatCurrency(Number(order.totalAmount || 0), currency)}
          </div>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="bg-background/70 backdrop-blur">
          <CardHeader>
            <CardTitle>Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2">
            {(order.items || []).length ? (
              (order.items || []).map((it, idx) => (
                <div key={String(it.id || idx)} className="rounded-xl border bg-background/60 px-3 py-2 text-sm">
                  <div className="font-medium">{it.productName || "Product"}</div>
                  <div className="text-xs text-muted-foreground">
                    Qty: {it.quantity || 1} - {formatCurrency(Number(it.lineTotal || 0), currency)}
                  </div>
                </div>
              ))
            ) : (
              <div className="text-sm text-muted-foreground">No items.</div>
            )}
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card className="bg-background/70 backdrop-blur">
            <CardHeader>
              <CardTitle>Payment</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <Button
                onClick={onPayWithMomo}
                disabled={isMomoLoading}
                className="h-10 w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
              >
                {isMomoLoading ? "Opening MoMo..." : "Pay with MoMo"}
              </Button>
              <div className="text-xs text-muted-foreground">
                Save this link to view your order later: it contains your guest access token.
              </div>
            </CardContent>
          </Card>

          <Card className="bg-background/70 backdrop-blur">
            <CardHeader>
              <CardTitle className="text-base">Payment history</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {payments.length ? (
                payments.map((p) => (
                  <div key={String(p.id)} className="rounded-xl border bg-background/60 px-3 py-2 text-sm">
                    <div className="font-medium">
                      {p.method || "Payment"} - {formatCurrency(Number(p.amount || 0), p.orderCurrency || currency)}
                    </div>
                    <div className="text-xs text-muted-foreground">
                      Status: {(p.status || "INITIATED").toUpperCase()} - Txn: {p.providerTxnId || "-"}
                    </div>
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
