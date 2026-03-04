import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import momoQrImage from "@/assets/img/momo.jpg";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { createMomoPayment } from "@/lib/momoApi";
import { getMyOrder, type OrderResponse } from "@/lib/orderApi";
import { createPayment, listPayments } from "@/lib/paymentApi";

export default function MomoQrPaymentPage() {
  const toast = useToast();
  const navigate = useNavigate();
  const params = useParams();
  const orderId = useMemo(() => Number(params.orderId ?? "0"), [params.orderId]);

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isMomoLoading, setIsMomoLoading] = useState(false);
  const [hasExistingPayment, setHasExistingPayment] = useState(false);
  const [momoApiQrUrl, setMomoApiQrUrl] = useState<string | null>(null);
  const isMomoSandboxEnabled = String(import.meta.env.VITE_MOMO_SANDBOX_ENABLED || "false").toLowerCase() === "true";

  const manualMomoAccountName =
    ((import.meta.env.VITE_MANUAL_MOMO_ACCOUNT_NAME as string | undefined) ||
      (import.meta.env.VITE_MOMO_ACCOUNT_NAME as string | undefined) ||
      "").trim() || "MoMo Account";
  const manualMomoAccountNumber =
    ((import.meta.env.VITE_MANUAL_MOMO_ACCOUNT_NUMBER as string | undefined) ||
      (import.meta.env.VITE_MOMO_ACCOUNT_NUMBER as string | undefined) ||
      "").trim() || "-";
  const transferContent = `DH${orderId}`;

  useEffect(() => {
    if (!orderId) return;
    let alive = true;
    setIsLoading(true);
    setError(null);
    Promise.all([getMyOrder(orderId), listPayments(orderId).catch(() => [])])
      .then(([o, payments]) => {
        if (!alive) return;
        setOrder(o);
        setHasExistingPayment((payments ?? []).length > 0);
      })
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load order.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [orderId]);

  async function onTransferred() {
    if (!orderId) return;
    if (hasExistingPayment) {
      toast.push({ variant: "error", title: "Payment exists", message: "This order already has a payment record." });
      return;
    }
    setIsSubmitting(true);
    try {
      await createPayment(orderId, {
        orderId,
        method: "MOMO_QR",
        status: "INITIATED",
        providerTxnId: transferContent,
      });
      toast.push({ variant: "success", title: "Payment submitted", message: "Payment is pending admin verification." });
      navigate(`/orders/${orderId}`, { replace: true });
    } catch (e) {
      toast.push({ variant: "error", title: "Submit failed", message: getErrorMessage(e, "Couldn't submit transfer confirmation.") });
    } finally {
      setIsSubmitting(false);
    }
  }

  async function onPayWithMomoSandbox() {
    if (!orderId) return;
    if (hasExistingPayment) {
      toast.push({ variant: "error", title: "Payment exists", message: "This order already has a payment record." });
      return;
    }
    setIsMomoLoading(true);
    try {
      const idempotencyKey =
        typeof crypto !== "undefined" && "randomUUID" in crypto ? crypto.randomUUID() : `momo_${Date.now()}`;
      const res = await createMomoPayment(orderId, idempotencyKey);
      const payUrl = res.payUrl || res.deeplink || res.qrCodeUrl;
      if (!payUrl) throw new Error(res.message || "No payment URL returned.");
      setMomoApiQrUrl(res.qrCodeUrl || null);
      window.open(payUrl, "_blank");
      toast.push({ variant: "success", title: "MoMo", message: "Opened payment in a new tab." });
    } catch (e) {
      toast.push({ variant: "error", title: "MoMo failed", message: getErrorMessage(e, "Couldn't create MoMo payment.") });
    } finally {
      setIsMomoLoading(false);
    }
  }

  if (!orderId) return <EmptyState title="Invalid order" description="Missing orderId." />;
  if (isLoading) return <div className="space-y-4"><LoadingCard /><LoadingCard /></div>;
  if (error || !order) {
    return (
      <EmptyState
        title="Couldn't load payment page"
        description={error || "Order not found."}
        action={
          <Button asChild className="rounded-xl bg-primary text-primary-foreground">
            <Link to="/orders">Back to orders</Link>
          </Button>
        }
      />
    );
  }

  const currency = order.currency || "VND";
  const amount = Number(order.totalAmount ?? 0);

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">MoMo QR Payment</div>
            <div className="text-2xl font-semibold">Order #{orderId}</div>
            <div className="mt-1 text-sm text-muted-foreground">
              Transfer exactly {formatCurrency(amount, currency)} with content {transferContent}.
            </div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-xl bg-background">
            <Link to={`/orders/${orderId}`}>Back to order</Link>
          </Button>
        </div>
      </section>

      <Card className="mx-auto w-full max-w-2xl bg-background">
        <CardHeader>
          <CardTitle>Scan and transfer</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {isMomoSandboxEnabled ? (
            <Button
              disabled={isMomoLoading || hasExistingPayment}
              onClick={onPayWithMomoSandbox}
              className="h-11 w-full rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isMomoLoading ? "Opening MoMo..." : "Pay with MoMo"}
            </Button>
          ) : null}
          <div className="overflow-hidden rounded-xl border bg-white p-3">
            <img
              src={momoApiQrUrl || momoQrImage}
              alt="MoMo QR payment"
              className="mx-auto max-h-[520px] w-auto object-contain"
            />
          </div>
          <div className="grid gap-2 rounded-xl border bg-background p-4 text-sm">
            <div><span className="text-muted-foreground">Account:</span> <span className="font-medium">{manualMomoAccountName}</span></div>
            <div><span className="text-muted-foreground">Number:</span> <span className="font-medium">{manualMomoAccountNumber}</span></div>
            <div><span className="text-muted-foreground">Amount:</span> <span className="font-medium">{formatCurrency(amount, currency)}</span></div>
            <div><span className="text-muted-foreground">Transfer content:</span> <span className="font-semibold">{transferContent}</span></div>
          </div>
          {hasExistingPayment ? (
            <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">
              Payment already exists for this order. You cannot submit MoMo QR transfer again.
            </div>
          ) : null}
          <Button
            disabled={isSubmitting || isMomoLoading || hasExistingPayment}
            onClick={onTransferred}
            className="h-11 w-full rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
          >
            {isSubmitting ? "Submitting..." : "I've transferred"}
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
