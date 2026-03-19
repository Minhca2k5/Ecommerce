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
import { createPayment, getPayment, listPayments, type PaymentResponse } from "@/lib/paymentApi";
import { createMomoPayment } from "@/lib/momoApi";
import { useNotifications } from "@/app/NotificationProvider";
import Modal from "@/components/Modal";
import { apiJson } from "@/lib/http";
import { createAuthedEventSource } from "@/lib/sse";

function dedupePayments(rows: PaymentResponse[]) {
  const map = new Map<number, PaymentResponse>();
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

type PaymentMethod = "MOMO_QR" | "MOMO_SANDBOX" | "COD" | "CARD";

function paymentMeta(method: PaymentMethod) {
  if (method === "MOMO_QR") return { label: "MoMo QR", icon: "◉" };
  if (method === "MOMO_SANDBOX") return { label: "MoMo", icon: "◉" };
  if (method === "COD") return { label: "Cash on Delivery", icon: "◈" };
  return { label: "Credit/Debit Card", icon: "◍" };
}

function getItemDisplayName(item: any, index: number) {
  const direct = String(item?.productName ?? "").trim();
  if (direct) return direct;
  const slug = String(item?.productSlug ?? "").trim();
  if (slug) return slug.replace(/[-_]+/g, " ").replace(/\s+/g, " ").trim();
  return `Item ${index + 1}`;
}

function getItemShortDescription(item: any) {
  const parts = [
    item?.variantName ? `Variant: ${String(item.variantName)}` : "",
    item?.color ? `Color: ${String(item.color)}` : "",
    item?.size ? `Size: ${String(item.size)}` : "",
  ].filter(Boolean);
  return parts.join(" • ");
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

  const [method, setMethod] = useState<PaymentMethod>("MOMO_QR");
  const [providerTxnId, setProviderTxnId] = useState("");
  const [isPaying, setIsPaying] = useState(false);
  const [isMomoLoading, setIsMomoLoading] = useState(false);
  const [isPaymentOpen, setIsPaymentOpen] = useState(false);
  const [paymentDetail, setPaymentDetail] = useState<PaymentResponse | null>(null);
  const [paymentDetailError, setPaymentDetailError] = useState<string | null>(null);
  const [isPaymentDetailLoading, setIsPaymentDetailLoading] = useState(false);


  const currency = order?.currency || "VND";
  const voucherId = useMemo(() => getNumber(order, "voucherId"), [order]);
  const discountAmount = useMemo(() => getNumber(order, "discountAmount") ?? 0, [order]);
  const subtotalAmount = useMemo(() => getNumber(order, "subtotalAmount") ?? 0, [order]);
  const shippingFee = useMemo(() => getNumber(order, "shippingFee") ?? 0, [order]);
  const taxAmount = useMemo(() => getNumber(order, "taxAmount") ?? 0, [order]);
  const isMomoSandboxEnabled = String(import.meta.env.VITE_MOMO_SANDBOX_ENABLED || "false").toLowerCase() === "true";
  const paymentMethods = useMemo<PaymentMethod[]>(
    () => (isMomoSandboxEnabled ? ["MOMO_QR", "MOMO_SANDBOX", "COD", "CARD"] : ["MOMO_QR", "COD", "CARD"]),
    [isMomoSandboxEnabled],
  );

  useEffect(() => {
    if (!orderId) return;
    let alive = true;
    setIsLoading(true);
    setError(null);
    Promise.all([getMyOrder(orderId), listPayments(orderId).catch(() => [])])
      .then(([o, p]) => {
        if (!alive) return;
        setOrder(o);
        setPayments(dedupePayments(p));
      })
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load order.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [orderId]);

  useEffect(() => {
    if (!orderId) return;
    const es = createAuthedEventSource("/api/users/me/realtime/orders");
    const reload = async () => {
      const [o, p] = await Promise.all([getMyOrder(orderId), listPayments(orderId).catch(() => [])]);
      setOrder(o);
      setPayments(dedupePayments(p));
    };
    const onPaymentStatus = () => void reload();
    es.addEventListener("order-status", onPaymentStatus);
    es.addEventListener("payment-status", onPaymentStatus);
    es.addEventListener("payment-created", onPaymentStatus);
    es.onerror = () => {
      // ignore
    };
    return () => {
      es.close();
    };
  }, [orderId]);

  async function onCreatePayment() {
    if (!orderId) return;
    setIsPaying(true);
    try {
      const resolvedProviderTxnId = providerTxnId.trim();
      await createPayment(orderId, {
        orderId,
        method,
        status: "INITIATED",
        ...(resolvedProviderTxnId ? { providerTxnId: resolvedProviderTxnId } : {}),
        ...(voucherId !== undefined ? { voucherId } : {}),
        ...(discountAmount !== undefined ? { discountAmount } : {}),
      });
      toast.push({ variant: "success", title: "Payment created", message: "Payment created for this order." });
      const itemNamesFrom = (list: any[]) =>
        (list ?? [])
          .map((it) => {
            const direct = String(it?.productName ?? it?.name ?? it?.title ?? "").trim();
            if (direct) return direct;
            const nested = String(it?.product?.name ?? it?.product?.title ?? it?.productNameSnapshot ?? it?.productSnapshotName ?? "").trim();
            return nested;
          })
          .filter(Boolean);

      let itemNames = itemNamesFrom((order?.items ?? []) as any[]);
      if (!itemNames.length) {
        const fromEndpoint = await apiJson<any[]>(`/api/users/me/orders/${orderId}/items/all`, { method: "GET", auth: true }).catch(() => []);
        itemNames = itemNamesFrom(fromEndpoint ?? []);
      }

      const preview = itemNames.slice(0, 3).join(", ");
      const suffix = itemNames.length > 3 ? ", ..." : "";
      notifications.push({
        type: "PAYMENT",
        title: "Payment created",
        message: preview
          ? `Items: ${preview}${suffix} • Method: ${method} • Tap to view details.`
          : `Payment created • Method: ${method} • Tap to view details.`,
        referenceId: orderId,
        referenceType: "ORDER",
      });
      setProviderTxnId("");
      const updated = await listPayments(orderId).catch(() => []);
      setPayments(dedupePayments(updated));
    } catch (e) {
      toast.push({ variant: "error", title: "Payment failed", message: getErrorMessage(e, "Couldn't create payment. Please try again later.") });
    } finally {
      setIsPaying(false);
    }
  }

  async function onSubmitPayment() {
    if (payments.length > 0) {
      toast.push({ variant: "error", title: "Payment exists", message: "This order already has a payment record." });
      return;
    }
    if (method === "MOMO_QR") {
      navigate(`/orders/${orderId}/momo-qr`);
      return;
    }
    if (method === "MOMO_SANDBOX") {
      if (!orderId) return;
      setIsMomoLoading(true);
      try {
        const idempotencyKey =
          typeof crypto !== "undefined" && "randomUUID" in crypto ? crypto.randomUUID() : `momo_${Date.now()}`;
        const res = await createMomoPayment(orderId, idempotencyKey);
        const payUrl = res.payUrl || res.deeplink || res.qrCodeUrl;
        if (!payUrl) {
          throw new Error(res.message || "No payment URL returned.");
        }
        window.open(payUrl, "_blank");
        toast.push({ variant: "success", title: "MoMo", message: "Opened payment in a new tab." });
      } catch (e) {
        toast.push({ variant: "error", title: "MoMo failed", message: getErrorMessage(e, "Couldn't create MoMo payment.") });
      } finally {
        setIsMomoLoading(false);
      }
      return;
    }
    await onCreatePayment();
  }


  async function openPayment(paymentId?: number) {
    const id = Number(paymentId ?? 0);
    if (!orderId || !id) return;
    setIsPaymentOpen(true);
    setPaymentDetail(null);
    setPaymentDetailError(null);
    setIsPaymentDetailLoading(true);
    try {
      const detail = await getPayment(orderId, id);
      setPaymentDetail(detail);
    } catch (e) {
      setPaymentDetailError(getErrorMessage(e, "Failed to load payment."));
    } finally {
      setIsPaymentDetailLoading(false);
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
          <Button asChild className="rounded-md bg-primary text-primary-foreground">
            <Link to="/orders">Back to orders</Link>
          </Button>
        }
      />
    );
  }

  const items = (order.items ?? []) as any[];

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Order</div>
            <div className="text-2xl font-semibold">Order details</div>
            <div className="mt-1 text-sm text-muted-foreground">
            {items.length} items • {formatCurrency(Number(order.totalAmount ?? 0), currency)} • {statusBadge(order.status)}
            </div>
        </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-md bg-background" onClick={() => navigate("/orders")}>
            Back
            </Button>
            <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground hover:bg-primary/90">
              <Link to="/products">Shop more</Link>
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_380px] lg:items-start">
        <Card className="bg-background">
          <CardHeader>
            <CardTitle className="text-base">Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {items.length ? (
              items.map((it, idx) => (
                <div key={String(it?.id ?? idx)} className="pressable group flex gap-3 rounded-md border bg-background p-3 shadow-sm transition hover:shadow-md">
                  <div className="h-16 w-16 overflow-hidden rounded-md border bg-muted">
                    <SafeImage
                      src={it?.url ?? ""}
                      alt={it?.productName ?? "Product"}
                      fallbackKey={String(it?.id ?? it?.productId ?? idx)}
                      className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
                    />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-medium">{getItemDisplayName(it, idx)}</div>
                    {getItemShortDescription(it) ? (
                      <div className="mt-1 line-clamp-1 text-xs text-muted-foreground">{getItemShortDescription(it)}</div>
                    ) : null}
                    <div className="mt-1 flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                      <span>Qty: {it?.quantity ?? 1}</span>
                      {it?.variantName ? <span>Variant: {String(it.variantName)}</span> : null}
                      {it?.color ? <span>Color: {String(it.color)}</span> : null}
                      {it?.size ? <span>Size: {String(it.size)}</span> : null}
                    </div>
                    <div className="mt-2">
                      <Button asChild variant="outline" className="rounded-md bg-background">
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

        <div className="space-y-4 lg:sticky lg:top-24">
          <Card className="bg-background">
            <CardHeader>
              <CardTitle>Order summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Subtotal</span><span>{formatCurrency(subtotalAmount, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Discount</span><span className={discountAmount > 0 ? "text-success" : ""}>- {formatCurrency(discountAmount, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Shipping</span><span>{formatCurrency(shippingFee, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Tax</span><span>{formatCurrency(taxAmount, currency)}</span></div>
              <div className="h-px bg-border" />
              <div className="flex items-center justify-between rounded-md bg-accent/60 px-3 py-2 font-semibold">
                <span className="text-base">Total</span>
                <span className="text-xl font-extrabold text-foreground">{formatCurrency(Number(order.totalAmount ?? 0), currency)}</span>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-background">
            <CardHeader>
              <CardTitle>Payment</CardTitle>
            </CardHeader>
            <CardContent className="space-y-3">
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Method</div>
                <div className="flex flex-wrap gap-2">
                  {paymentMethods.map((m) => (
                    <button
                      key={m}
                      type="button"
                      onClick={() => setMethod(m)}
                      className={[
                        "pressable inline-flex items-center gap-2 rounded-full border px-3 py-1.5 text-xs shadow-sm transition",
                        method === m
                          ? "border-primary bg-primary/10 text-foreground ring-1 ring-primary/30"
                          : "bg-background hover:bg-muted",
                      ].join(" ")}
                    >
                      <span className={method === m ? "text-primary" : "text-muted-foreground"}>{paymentMeta(m).icon}</span>
                      {paymentMeta(m).label}
                    </button>
                  ))}
                </div>
              </div>
              <div className="space-y-2">
                  <div className="text-xs font-medium text-muted-foreground">Provider txn id (optional)</div>
                  <Input className="rounded-md bg-background" value={providerTxnId} onChange={(e) => setProviderTxnId(e.target.value)} placeholder="e.g. VNPAY-..." />
                </div>
              <Button
                disabled={isPaying || isMomoLoading}
                onClick={onSubmitPayment}
                className="h-10 w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
              >
                {isPaying
                  ? "Creating..."
                  : isMomoLoading
                    ? "Opening MoMo..."
                    : method === "MOMO_QR"
                      ? "Continue to payment"
                      : method === "MOMO_SANDBOX"
                        ? "Pay now"
                        : method === "CARD"
                          ? "Pay now"
                          : "Place order"}
              </Button>
              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs text-emerald-800">
                Secure payment • Buyer protection • Easy returns
              </div>
            </CardContent>
          </Card>

          <Card className="bg-background">
            <CardHeader>
              <CardTitle className="text-base">Payments</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {payments.length ? (
                payments.map((p) => (
                  <button
                    key={String(p.id)}
                    type="button"
                    className="pressable flex w-full items-center justify-between rounded-md border bg-background px-3 py-2 text-left shadow-sm transition hover:bg-muted hover:shadow-md"
                    onClick={() => void openPayment(Number(p.id ?? 0))}
                  >
                    <div className="min-w-0">
                      <div className="text-sm font-medium">{p.method || "Payment"} • {formatCurrency(Number(p.amount ?? 0), p.orderCurrency || currency)}</div>
                      <div className="text-xs text-muted-foreground truncate">{p.providerTxnId || "-"}</div>
                    </div>
                    <span className="rounded-full bg-muted px-2 py-1 text-xs text-muted-foreground ring-1 ring-border">{(p.status || "INITIATED").toUpperCase()}</span>
                  </button>
                ))
              ) : (
                <div className="text-sm text-muted-foreground">No payments yet.</div>
              )}
            </CardContent>
          </Card>
        </div>
      </div>

      <Modal isOpen={isPaymentOpen} onClose={() => setIsPaymentOpen(false)} title="Payment details">
        <div className="space-y-3">
          {isPaymentDetailLoading ? <div className="text-sm text-muted-foreground">Loading...</div> : null}
          {paymentDetailError ? (
            <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{paymentDetailError}</div>
          ) : null}
          {paymentDetail ? (
            <div className="grid gap-2 text-sm">
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Payment</span><span className="font-medium">Payment details</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Method</span><span className="font-medium">{paymentDetail.method || "-"}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Status</span><span className="font-medium">{String(paymentDetail.status || "-").toUpperCase()}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Amount</span><span className="font-medium">{formatCurrency(Number(paymentDetail.amount ?? 0), paymentDetail.orderCurrency || currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Provider txn</span><span className="font-medium truncate max-w-[220px]">{paymentDetail.providerTxnId || "-"}</span></div>
            </div>
          ) : null}
          <Button variant="outline" className="h-10 w-full rounded-md bg-background" onClick={() => setIsPaymentOpen(false)}>
            Close
          </Button>
        </div>
      </Modal>
    </div>
  );
}

