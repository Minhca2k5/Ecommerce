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

const QR_EXPIRE_MS = 5 * 60 * 1000;

function formatCountdown(ms: number) {
  const total = Math.max(0, Math.floor(ms / 1000));
  const m = Math.floor(total / 60);
  const s = total % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function dedupeById<T extends { id?: number | null }>(rows: T[]) {
  const map = new Map<number, T>();
  for (const row of rows) {
    const id = Number(row?.id ?? 0);
    if (!id) continue;
    if (!map.has(id)) map.set(id, row);
  }
  return Array.from(map.values());
}

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
  const [expiresAt, setExpiresAt] = useState(Date.now() + QR_EXPIRE_MS);
  const [nowMs, setNowMs] = useState(Date.now());
  const [isRefreshingStatus, setIsRefreshingStatus] = useState(false);
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
        const deduped = dedupeById(payments ?? []);
        setOrder(o);
        setHasExistingPayment(deduped.length > 0);
        setExpiresAt(Date.now() + QR_EXPIRE_MS);
      })
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load order.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [orderId]);

  useEffect(() => {
    const id = window.setInterval(() => setNowMs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, []);

  async function refreshStatus() {
    if (!orderId) return;
    setIsRefreshingStatus(true);
    try {
      const [o, payments] = await Promise.all([getMyOrder(orderId), listPayments(orderId).catch(() => [])]);
      const deduped = dedupeById(payments ?? []);
      setOrder(o);
      setHasExistingPayment(deduped.length > 0);
      if (deduped.length > 0) {
        toast.push({ variant: "success", title: "Payment detected", message: "A payment record was found for this order." });
      } else {
        toast.push({ variant: "default", title: "No payment yet", message: "Please complete transfer then check again." });
      }
    } catch (e) {
      toast.push({ variant: "error", title: "Refresh failed", message: getErrorMessage(e, "Could not refresh payment status.") });
    } finally {
      setIsRefreshingStatus(false);
    }
  }

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
          <Button asChild className="rounded-md bg-primary text-primary-foreground">
            <Link to="/orders">Back to orders</Link>
          </Button>
        }
      />
    );
  }

  const currency = order.currency || "VND";
  const amount = Number(order.totalAmount ?? 0);
  const remainingMs = Math.max(0, expiresAt - nowMs);
  const isExpired = remainingMs <= 0;
  const countdown = formatCountdown(remainingMs);

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-md border border-fuchsia-200 bg-gradient-to-br from-fuchsia-50 via-rose-50 to-violet-100 p-5 shadow-sm">
        <div className="pointer-events-none absolute -left-12 -top-16 h-48 w-48 rounded-full bg-fuchsia-300/35 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-16 -right-10 h-48 w-48 rounded-full bg-violet-300/35 blur-3xl" />
        <div className="relative z-10 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="space-y-1">
            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-fuchsia-700">Payment in progress</div>
            <div className="text-2xl font-semibold text-slate-900">Order #{orderId} · {formatCurrency(amount, currency)}</div>
            <div className="text-sm text-slate-700">
              Transfer exactly <span className="font-semibold">{formatCurrency(amount, currency)}</span> with content <span className="font-semibold">{transferContent}</span>.
            </div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-md border-fuchsia-200 bg-white/90">
            <Link to={`/orders/${orderId}`}>Back to order</Link>
          </Button>
        </div>
      </section>

      <Card className="mx-auto w-full max-w-3xl overflow-hidden border-fuchsia-200 bg-white/95 shadow-[0_24px_60px_-34px_rgba(190,24,93,0.55)] backdrop-blur-sm">
        <CardHeader className="border-b bg-gradient-to-r from-fuchsia-600 to-rose-500 text-white">
          <CardTitle className="flex flex-wrap items-center justify-between gap-2 text-base">
            <span>Waiting for payment...</span>
            <span className={["rounded-full px-3 py-1 text-sm font-semibold", isExpired ? "bg-rose-100 text-rose-700" : "bg-white/20 text-white"].join(" ")}>
              {isExpired ? "QR expired" : `This QR will expire in ${countdown}`}
            </span>
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-5 p-5">
          {isMomoSandboxEnabled ? (
            <Button
              disabled={isMomoLoading || hasExistingPayment}
              onClick={onPayWithMomoSandbox}
              className="h-11 w-full rounded-md bg-fuchsia-600 text-white hover:bg-fuchsia-700"
            >
              {isMomoLoading ? "Opening MoMo app..." : "Open MoMo app"}
            </Button>
          ) : null}

          <div className="grid gap-4 md:grid-cols-[1.2fr_0.8fr] md:items-start">
            <div className="rounded-md border border-fuchsia-100 bg-white p-4 shadow-[0_16px_34px_-24px_rgba(190,24,93,0.7)]">
              <div className="mx-auto w-full max-w-[420px] overflow-hidden rounded-md border-4 border-white bg-white shadow-[0_22px_50px_-26px_rgba(15,23,42,0.7)]">
                <img
                  src={momoApiQrUrl || momoQrImage}
                  alt="MoMo QR payment"
                  className="mx-auto max-h-[620px] w-full object-contain"
                />
              </div>
              <div className="mt-3 text-center text-sm text-muted-foreground">Scan this code with your MoMo app to complete payment.</div>
            </div>

            <div className="space-y-3">
              <div className="grid gap-2 rounded-md border border-fuchsia-100 bg-fuchsia-50/70 p-4 text-sm">
                <div><span className="text-muted-foreground">Account:</span> <span className="font-medium">{manualMomoAccountName}</span></div>
                <div><span className="text-muted-foreground">Number:</span> <span className="font-medium">{manualMomoAccountNumber}</span></div>
                <div><span className="text-muted-foreground">Amount:</span> <span className="font-semibold text-slate-900">{formatCurrency(amount, currency)}</span></div>
                <div><span className="text-muted-foreground">Transfer content:</span> <span className="font-semibold text-slate-900">{transferContent}</span></div>
              </div>

              <Button
                disabled={isSubmitting || isMomoLoading || hasExistingPayment || isExpired}
                onClick={onTransferred}
                className="h-11 w-full rounded-md bg-fuchsia-600 text-white hover:bg-fuchsia-700"
              >
                {isSubmitting ? "Confirming..." : "I have paid"}
              </Button>

              <Button
                type="button"
                variant="outline"
                disabled={isRefreshingStatus || isSubmitting}
                onClick={() => void refreshStatus()}
                className="h-11 w-full rounded-md border-fuchsia-200 bg-white"
              >
                {isRefreshingStatus ? "Checking..." : "Check payment"}
              </Button>

              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm text-emerald-800">
                Secure and verified. Your order will update once payment is confirmed.
              </div>
            </div>
          </div>

          <div className="grid gap-2 rounded-md border border-slate-200 bg-slate-50 px-4 py-3 text-sm sm:grid-cols-3">
            <div className="font-medium text-slate-700">🔒 Secure payment</div>
            <div className="font-medium text-slate-700">✅ Verified by MoMo</div>
            <div className="font-medium text-slate-700">🛡 Refund guarantee</div>
          </div>

          {isExpired ? (
            <div className="rounded-md border border-amber-300 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              QR has expired. Reload this page to generate a fresh payment session.
            </div>
          ) : null}
          {hasExistingPayment ? (
            <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">
              Payment already exists for this order. You cannot submit transfer again.
            </div>
          ) : null}
        </CardContent>
      </Card>
    </div>
  );
}

