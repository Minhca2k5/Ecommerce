import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { getOrCreateCart, type CartResponse } from "@/lib/cartApi";
import { createMyOrder } from "@/lib/orderApi";
import { getDefaultAddress, listAddresses, type AddressResponse } from "@/lib/userApi";
import { useNotifications } from "@/app/NotificationProvider";
import { getVouchersByCode, type VoucherResponse } from "@/lib/voucherApi";

function money(value: number | undefined, currency: string | undefined) {
  if (value === undefined || value === null) return "-";
  return formatCurrency(value, currency || "VND");
}

export default function CheckoutPage() {
  const toast = useToast();
  const navigate = useNavigate();
  const notifications = useNotifications();

  const [cart, setCart] = useState<CartResponse | null>(null);
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [shippingFee, setShippingFee] = useState<string>("0");
  const [voucherCode, setVoucherCode] = useState<string>("");
  const [voucherStatus, setVoucherStatus] = useState<"idle" | "checking" | "valid" | "invalid">("idle");
  const [voucherError, setVoucherError] = useState<string | null>(null);
  const [appliedVoucher, setAppliedVoucher] = useState<VoucherResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const currency = cart?.currency || "VND";
  const total = Number(cart?.totalAmount ?? cart?.itemsSubtotal ?? 0) + Math.max(0, Number(shippingFee || "0"));

  const canSubmit = useMemo(() => {
    if (isSubmitting) return false;
    if (!cart?.id) return false;
    if (!cart.items?.length) return false;
    if (!selectedAddressId) return false;
    return true;
  }, [cart?.id, cart?.items?.length, isSubmitting, selectedAddressId]);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);

    Promise.all([getOrCreateCart(), listAddresses().catch(() => [] as AddressResponse[]), getDefaultAddress().catch(() => null as AddressResponse | null)])
      .then(([c, list, def]) => {
        if (!alive) return;
        setCart(c);
        setAddresses(list);
        const defaultId = def?.id ?? list.find((a) => a.isDefault)?.id ?? list[0]?.id ?? null;
        setSelectedAddressId(defaultId ? Number(defaultId) : null);
      })
      .catch((e) => {
        if (!alive) return;
        setError(getErrorMessage(e, "Failed to load checkout."));
      })
      .finally(() => alive && setIsLoading(false));

    return () => {
      alive = false;
    };
  }, []);

  async function placeOrder() {
    if (!cart?.id || !selectedAddressId) return;
    setIsSubmitting(true);
    try {
      const voucherId = appliedVoucher?.id ? Number(appliedVoucher.id) : undefined;
      const created = await createMyOrder({
        cartId: Number(cart.id),
        addressIdSnapshot: Number(selectedAddressId),
        ...(voucherId ? { voucherId } : {}),
        shippingFee: Math.max(0, Number(shippingFee || "0")),
        currency,
        status: "PENDING",
      });

      toast.push({ variant: "success", title: "Order placed", message: "Your order has been created." });

      const orderId = Number(created.id ?? 0);
      if (orderId) {
        notifications.push({
          type: "ORDER",
          title: `Order #${orderId} created`,
          message: `Your order is pending. Total: ${formatCurrency(Number(created.totalAmount ?? 0), created.currency || currency)}.`,
          referenceId: orderId,
          referenceType: "ORDER",
        });

        if (voucherId) {
          notifications.push({
            type: "VOUCHER",
            title: "Voucher applied",
            message: `Applied code ${appliedVoucher?.code || ""} to your order.`,
            referenceId: voucherId,
            referenceType: "VOUCHER",
          });
        }
      }
      navigate(orderId ? `/orders/${orderId}` : "/orders", { replace: true });
    } catch (e) {
      toast.push({ variant: "error", title: "Checkout failed", message: getErrorMessage(e, "Failed to place order.") });
    } finally {
      setIsSubmitting(false);
    }
  }

  async function checkVoucher() {
    const code = voucherCode.trim();
    setAppliedVoucher(null);
    setVoucherError(null);
    if (!code) {
      setVoucherStatus("idle");
      return;
    }
    setVoucherStatus("checking");
    try {
      const list = await getVouchersByCode(code);
      const voucher = (list ?? [])[0] ?? null;
      if (!voucher?.id) {
        setVoucherStatus("invalid");
        setVoucherError("Voucher not found or inactive.");
        return;
      }
      setAppliedVoucher(voucher);
      setVoucherStatus("valid");
      notifications.push({
        type: "VOUCHER",
        title: "Voucher found",
        message: `Code ${voucher.code} is ready to use at checkout.`,
        referenceId: Number(voucher.id),
        referenceType: "VOUCHER",
      });
    } catch (e) {
      setVoucherStatus("invalid");
      setVoucherError(getErrorMessage(e, "Failed to validate voucher."));
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

  if (error) {
    return (
      <EmptyState
        title="Couldn't load checkout"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            Retry
          </Button>
        }
      />
    );
  }

  if (!cart?.items?.length) {
    return (
      <EmptyState
        title="Nothing to checkout"
        description="Your cart is empty."
        action={
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/products">Browse products</Link>
          </Button>
        }
      />
    );
  }

  if (!addresses.length) {
    return (
      <EmptyState
        title="Add an address first"
        description="Checkout requires at least one delivery address."
        action={
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/me/addresses">Go to address book</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Checkout</div>
          <div className="text-3xl font-semibold tracking-tight">Place your order</div>
          <div className="mt-1 text-sm text-muted-foreground">Confirm address and create the order.</div>
        </div>
        <Button asChild variant="outline" className="rounded-xl">
          <Link to="/cart">Back to cart</Link>
        </Button>
      </div>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="space-y-4">
          <Card className="overflow-hidden">
            <CardHeader className="relative">
              <CardTitle>Delivery address</CardTitle>
            </CardHeader>
            <CardContent className="relative space-y-3">
              <div className="grid gap-2">
                {addresses.map((a) => {
                  const id = Number(a.id ?? 0);
                  const active = selectedAddressId === id;
                  return (
                    <button
                      key={String(a.id)}
                      type="button"
                      onClick={() => setSelectedAddressId(id)}
                      className={[
                        "rounded-2xl border p-3 text-left transition pressable",
                        active ? "border-primary bg-primary/10 ring-1 ring-primary/20" : "bg-background/70 hover:bg-muted",
                      ].join(" ")}
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="font-medium">{a.city || a.country || "Address"}</div>
                        {a.isDefault ? (
                          <span className="rounded-full bg-emerald-500/10 px-2 py-1 text-xs text-emerald-700 ring-1 ring-emerald-500/20">Default</span>
                        ) : null}
                      </div>
                      <div className="mt-1 text-sm text-muted-foreground">{[a.line1, a.line2, a.state, a.zipcode, a.country].filter(Boolean).join(", ")}</div>
                    </button>
                  );
                })}
              </div>
              <Button asChild variant="outline" className="rounded-xl">
                <Link to="/me/addresses">Manage addresses</Link>
              </Button>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle>Extras</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Shipping fee</div>
                <Input className="rounded-xl" value={shippingFee} onChange={(e) => setShippingFee(e.target.value)} inputMode="numeric" />
              </div>
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Voucher code (optional)</div>
                <div className="flex gap-2">
                  <Input className="rounded-xl" value={voucherCode} onChange={(e) => setVoucherCode(e.target.value)} placeholder="e.g. SAVE10" />
                  <Button type="button" variant="outline" className="rounded-xl" onClick={checkVoucher} disabled={voucherStatus === "checking" || !voucherCode.trim()}>
                    {voucherStatus === "checking" ? "Checking..." : "Apply"}
                  </Button>
                </div>
                {voucherStatus === "valid" && appliedVoucher ? (
                  <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/10 px-3 py-2 text-xs text-emerald-700">
                    Applied: <span className="font-medium">{appliedVoucher.code}</span> — {appliedVoucher.name || "Voucher"}
                  </div>
                ) : null}
                {voucherStatus === "invalid" && voucherError ? (
                  <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-xs text-rose-700">{voucherError}</div>
                ) : null}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="sticky top-24 overflow-hidden">
          <CardHeader className="relative">
            <CardTitle>Order summary</CardTitle>
          </CardHeader>
          <CardContent className="relative space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Subtotal</span>
              <span>{money(Number(cart.itemsSubtotal ?? 0), currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Shipping</span>
              <span>{money(Math.max(0, Number(shippingFee || "0")), currency)}</span>
            </div>
            <div className="h-px bg-border" />
            <div className="flex items-center justify-between text-base font-semibold">
              <span>Total</span>
              <span>{money(total, currency)}</span>
            </div>

            <Button disabled={!canSubmit} onClick={placeOrder} className="w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
              {isSubmitting ? "Placing..." : "Place order"}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
