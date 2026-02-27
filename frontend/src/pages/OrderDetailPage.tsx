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

  const [method, setMethod] = useState("MOMO_QR");
  const [providerTxnId, setProviderTxnId] = useState("");
  const [isPaying, setIsPaying] = useState(false);
  const [isMomoLoading, setIsMomoLoading] = useState(false);
  const [isPaymentOpen, setIsPaymentOpen] = useState(false);
  const [paymentDetail, setPaymentDetail] = useState<PaymentResponse | null>(null);
  const [paymentDetailError, setPaymentDetailError] = useState<string | null>(null);
  const [isPaymentDetailLoading, setIsPaymentDetailLoading] = useState(false);

  const [itemsMode, setItemsMode] = useState<"embedded" | "endpoint">("embedded");
  const [endpointItems, setEndpointItems] = useState<any[] | null>(null);
  const [isItemsLoading, setIsItemsLoading] = useState(false);
  const [itemsError, setItemsError] = useState<string | null>(null);

  const currency = order?.currency || "VND";
  const voucherId = useMemo(() => getNumber(order, "voucherId"), [order]);
  const discountAmount = useMemo(() => getNumber(order, "discountAmount") ?? 0, [order]);
  const subtotalAmount = useMemo(() => getNumber(order, "subtotalAmount") ?? 0, [order]);
  const shippingFee = useMemo(() => getNumber(order, "shippingFee") ?? 0, [order]);
  const taxAmount = useMemo(() => getNumber(order, "taxAmount") ?? 0, [order]);
  const isMomoSandboxEnabled = String(import.meta.env.VITE_MOMO_SANDBOX_ENABLED || "false").toLowerCase() === "true";
  const paymentMethods = useMemo(
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
        setPayments(p);
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
      setPayments(p);
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
      setPayments(updated);
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
        toast.push({ variant: "success", title: "MoMo sandbox", message: "Opened sandbox payment in a new tab." });
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

  async function loadItemsFromEndpoint() {
    if (!orderId) return;
    setItemsMode("endpoint");
    setIsItemsLoading(true);
    setItemsError(null);
    try {
      const list = await apiJson<any[]>(`/api/users/me/orders/${orderId}/items/all`, { method: "GET", auth: true });
      setEndpointItems(list ?? []);
    } catch (e) {
      setItemsError(getErrorMessage(e, "Failed to load order items."));
      setEndpointItems([]);
    } finally {
      setIsItemsLoading(false);
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

  const embeddedItems = (order.items ?? []) as any[];
  const items = itemsMode === "endpoint" ? (endpointItems ?? []) : embeddedItems;

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Order</div>
            <div className="text-3xl font-semibold tracking-tight">Order details</div>
            <div className="mt-1 text-sm text-muted-foreground">
            {items.length} items • {formatCurrency(Number(order.totalAmount ?? 0), currency)} • {statusBadge(order.status)}
            </div>
        </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur" onClick={() => navigate("/orders")}>
            Back
            </Button>
            <Button asChild className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
              <Link to="/products">Shop more</Link>
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="shine bg-background/70 backdrop-blur">
          <CardHeader>
            <CardTitle className="flex items-center justify-between gap-2 text-base">
              <span>Items</span>
              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="h-8 rounded-lg bg-background/70 px-2 text-xs backdrop-blur"
                  onClick={() => {
                    setItemsMode("embedded");
                    setEndpointItems(null);
                    setItemsError(null);
                  }}
                  disabled={itemsMode === "embedded"}
                >
                  Embedded
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  className="h-8 rounded-lg bg-background/70 px-2 text-xs backdrop-blur"
                  onClick={() => void loadItemsFromEndpoint()}
                  disabled={isItemsLoading || itemsMode === "endpoint"}
                >
                  Load via API
                </Button>
              </div>
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            {itemsMode === "endpoint" && isItemsLoading ? (
              <div className="text-sm text-muted-foreground">Loading items...</div>
            ) : itemsMode === "endpoint" && itemsError ? (
              <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{itemsError}</div>
            ) : items.length ? (
              items.map((it, idx) => (
                <div key={String(it?.id ?? idx)} className="pressable group flex gap-3 rounded-2xl border bg-background/70 p-3 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-lg">
                  <div className="h-16 w-16 overflow-hidden rounded-xl border bg-muted">
                    <SafeImage
                      src={it?.url ?? ""}
                      alt={it?.productName ?? "Product"}
                      fallbackKey={String(it?.id ?? it?.productId ?? idx)}
                      className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
                    />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="truncate font-medium">{it?.productName ?? "Product"}</div>
                    <div className="mt-1 text-xs text-muted-foreground">Qty: {it?.quantity ?? 1}</div>
                    <div className="mt-2">
                      <Button asChild variant="outline" className="rounded-xl bg-background/70 backdrop-blur">
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
          <Card className="bg-background/70 backdrop-blur">
            <CardHeader>
              <CardTitle>Order summary</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Subtotal</span><span>{formatCurrency(subtotalAmount, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Discount</span><span className={discountAmount > 0 ? "text-emerald-600" : ""}>- {formatCurrency(discountAmount, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Shipping</span><span>{formatCurrency(shippingFee, currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Tax</span><span>{formatCurrency(taxAmount, currency)}</span></div>
              <div className="h-px bg-border" />
              <div className="flex items-center justify-between font-semibold"><span>Total</span><span>{formatCurrency(Number(order.totalAmount ?? 0), currency)}</span></div>
            </CardContent>
          </Card>

          <Card className="bg-background/70 backdrop-blur">
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
                        "pressable rounded-full border px-3 py-1 text-xs shadow-sm transition hover:-translate-y-0.5",
                        method === m ? "border-primary bg-primary text-primary-foreground" : "bg-background hover:bg-muted",
                      ].join(" ")}
                    >
                      {m}
                    </button>
                  ))}
                </div>
              </div>
              {method !== "MOMO_QR" && method !== "MOMO_SANDBOX" ? (
                <div className="space-y-2">
                  <div className="text-xs font-medium text-muted-foreground">Provider txn id (optional)</div>
                  <Input className="rounded-xl bg-background/70 backdrop-blur" value={providerTxnId} onChange={(e) => setProviderTxnId(e.target.value)} placeholder="e.g. VNPAY-..." />
                </div>
              ) : (
                <div className="rounded-2xl border bg-background/60 p-3 text-sm text-muted-foreground">
                  You will be redirected to a dedicated MoMo QR payment page.
                </div>
              )}
              <Button
                disabled={isPaying || isMomoLoading}
                onClick={onSubmitPayment}
                className="h-10 w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
              >
                {isPaying
                  ? "Creating..."
                  : isMomoLoading
                    ? "Opening sandbox..."
                    : method === "MOMO_QR"
                      ? "Continue to MoMo QR"
                      : method === "MOMO_SANDBOX"
                        ? "Pay with MoMo Sandbox"
                        : "Create payment"}
              </Button>
              <div className="text-xs text-muted-foreground">
                {method === "MOMO_QR"
                  ? "Open a separate page with QR and transfer details. Admin verification is still required."
                  : method === "MOMO_SANDBOX"
                    ? "Calls MoMo sandbox and opens payment URL in a new tab."
                  : "Creates a payment record via backend; gateway integration can be added later."}
              </div>
            </CardContent>
          </Card>

          <Card className="bg-background/70 backdrop-blur">
            <CardHeader>
              <CardTitle className="text-base">Payments</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2">
              {payments.length ? (
                payments.map((p) => (
                  <button
                    key={String(p.id)}
                    type="button"
                    className="pressable flex w-full items-center justify-between rounded-xl border bg-background/60 px-3 py-2 text-left shadow-sm transition hover:-translate-y-0.5 hover:bg-muted hover:shadow-md"
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
            <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{paymentDetailError}</div>
          ) : null}
          {paymentDetail ? (
            <div className="grid gap-2 text-sm">
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Payment</span><span className="font-medium">Details</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Method</span><span className="font-medium">{paymentDetail.method || "-"}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Status</span><span className="font-medium">{String(paymentDetail.status || "-").toUpperCase()}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Amount</span><span className="font-medium">{formatCurrency(Number(paymentDetail.amount ?? 0), paymentDetail.orderCurrency || currency)}</span></div>
              <div className="flex items-center justify-between"><span className="text-muted-foreground">Provider txn</span><span className="font-medium truncate max-w-[220px]">{paymentDetail.providerTxnId || "-"}</span></div>
            </div>
          ) : null}
          <Button variant="outline" className="h-10 w-full rounded-xl bg-background/70 backdrop-blur" onClick={() => setIsPaymentOpen(false)}>
            Close
          </Button>
        </div>
      </Modal>
    </div>
  );
}
