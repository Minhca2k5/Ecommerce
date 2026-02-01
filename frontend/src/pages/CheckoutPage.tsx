import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { getOrCreateCart, type CartResponse } from "@/lib/cartApi";
import { createMyOrder, getMyVoucherDiscount } from "@/lib/orderApi";
import { getDefaultAddress, listAddresses, type AddressResponse } from "@/lib/userApi";
import { filterMyVouchersByMinOrderAmount, getMyVouchersByCode, type VoucherResponse } from "@/lib/voucherApi";

function money(value: number, currency: string) {
  return formatCurrency(Number.isFinite(value) ? value : 0, currency || "VND");
}

function toNumber(value: number | string | undefined) {
  if (value === undefined || value === null) return 0;
  const n = typeof value === "string" ? Number(value) : value;
  return Number.isFinite(n) ? n : 0;
}

function voucherDiscountLabel(v: VoucherResponse) {
  const type = (v.discountType || "").toUpperCase();
  if (type === "FREE_SHIPPING") return "Free shipping";
  if (type === "PERCENT") {
    const pct = toNumber(v.discountValue);
    const max = v.maxDiscountAmount !== undefined && v.maxDiscountAmount !== null ? ` (max ${formatCurrency(toNumber(v.maxDiscountAmount), "VND")})` : "";
    return `${pct}%${max}`;
  }
  if (type === "FIXED") return formatCurrency(toNumber(v.discountValue), "VND");
  return type || "Voucher";
}

export default function CheckoutPage() {
  const auth = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const notifications = useNotifications();

  const [cart, setCart] = useState<CartResponse | null>(null);
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);
  const [selectedAddressId, setSelectedAddressId] = useState<number | null>(null);
  const [shippingFee, setShippingFee] = useState<string>("0");

  const [appliedVoucher, setAppliedVoucher] = useState<VoucherResponse | null>(null);
  const [isVoucherPickerOpen, setIsVoucherPickerOpen] = useState(false);
  const [voucherView, setVoucherView] = useState<"eligible" | "search">("eligible");

  const [eligiblePage, setEligiblePage] = useState(0);
  const [eligibleTotalPages, setEligibleTotalPages] = useState(1);
  const [eligibleItems, setEligibleItems] = useState<VoucherResponse[]>([]);
  const [eligibleLoading, setEligibleLoading] = useState(false);
  const [eligibleError, setEligibleError] = useState<string | null>(null);

  const [searchCode, setSearchCode] = useState("");
  const [searchResults, setSearchResults] = useState<VoucherResponse[]>([]);
  const [searchLoading, setSearchLoading] = useState(false);
  const [searchError, setSearchError] = useState<string | null>(null);

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [discountAmount, setDiscountAmount] = useState<number>(0);
  const [discountStatus, setDiscountStatus] = useState<"idle" | "loading" | "error">("idle");
  const [discountError, setDiscountError] = useState<string | null>(null);

  const currency = cart?.currency || "VND";
  const subtotal = Number(cart?.itemsSubtotal ?? cart?.totalAmount ?? 0);
  const shipping = Math.max(0, Number(shippingFee || "0"));
  const originTotal = subtotal + shipping;
  const discount = Math.max(0, Number(discountAmount || 0));
  const total = Math.max(0, originTotal - discount);

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

    if (!auth.isAuthenticated) {
      setIsLoading(false);
      return () => {
        alive = false;
      };
    }

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
  }, [auth.isAuthenticated]);

  if (!auth.isAuthenticated) {
    return (
      <EmptyState
        title="Login required"
        description="Please sign in to proceed with checkout."
        action={
          <Button asChild className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/login">Go to login</Link>
          </Button>
        }
      />
    );
  }

  async function loadEligibleVouchers(nextPage: number) {
    setEligibleLoading(true);
    setEligibleError(null);
    try {
      const res = await filterMyVouchersByMinOrderAmount(originTotal, { page: nextPage, size: 5 });
      setEligibleItems(res.content ?? []);
      setEligibleTotalPages(Number(res.totalPages ?? 1) || 1);
      setEligiblePage(nextPage);
    } catch (e) {
      setEligibleError(getErrorMessage(e, "Failed to load eligible vouchers."));
    } finally {
      setEligibleLoading(false);
    }
  }

  async function refreshDiscount(nextVoucherId?: number | null) {
    if (!cart?.id || !selectedAddressId) return;
    const voucherId = nextVoucherId ?? (appliedVoucher?.id ? Number(appliedVoucher.id) : null);
    setDiscountStatus("loading");
    setDiscountError(null);
    try {
      const value = await getMyVoucherDiscount({
        cartId: Number(cart.id),
        addressIdSnapshot: Number(selectedAddressId),
        shippingFee: shipping,
        currency,
        status: "PENDING",
        ...(voucherId ? { voucherId: Number(voucherId) } : {}),
      });
      setDiscountAmount(Math.max(0, Number(value ?? 0)));
      setDiscountStatus("idle");
    } catch (e) {
      setDiscountStatus("error");
      setDiscountError(getErrorMessage(e, "Failed to calculate discount."));
      setDiscountAmount(0);
    }
  }

  async function runVoucherSearch() {
    const code = searchCode.trim();
    if (!code) return;
    setVoucherView("search");
    setSearchLoading(true);
    setSearchError(null);
    try {
      const res = await getMyVouchersByCode(code);
      setSearchResults(res ?? []);
    } catch (e) {
      setSearchError(getErrorMessage(e, "Search failed."));
    } finally {
      setSearchLoading(false);
    }
  }

  async function resetVoucherSearch() {
    setSearchCode("");
    setSearchResults([]);
    setSearchError(null);
    setVoucherView("eligible");
    await loadEligibleVouchers(0);
  }

  useEffect(() => {
    if (!cart?.id || !selectedAddressId) return;
    const t = window.setTimeout(() => void refreshDiscount(null), 250);
    return () => window.clearTimeout(t);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cart?.id, selectedAddressId, shippingFee, appliedVoucher?.id]);

  async function placeOrder() {
    if (!cart?.id || !selectedAddressId) return;
    setIsSubmitting(true);
    try {
      const itemNames = (cart?.items ?? [])
        .map((it: any) => String(it?.productName ?? it?.name ?? it?.title ?? "").trim())
        .filter(Boolean);
      const preview = itemNames.slice(0, 3).join(", ");
      const suffix = itemNames.length > 3 ? ", ..." : "";

      const voucherId = appliedVoucher?.id ? Number(appliedVoucher.id) : undefined;
      const created = await createMyOrder({
        cartId: Number(cart.id),
        addressIdSnapshot: Number(selectedAddressId),
        ...(voucherId ? { voucherId } : {}),
        shippingFee: shipping,
        currency,
        status: "PENDING",
      });

      toast.push({ variant: "success", title: "Order placed", message: "Your order has been created." });

      const orderId = Number(created.id ?? 0);
      if (orderId) {
        notifications.push({
          type: "ORDER",
          title: "Order created",
          message: `Items: ${preview || "Your cart"}${suffix} • Total: ${formatCurrency(Number(created.totalAmount ?? 0), created.currency || currency)} • Tap to view details.`,
          referenceId: orderId,
          referenceType: "ORDER",
        });
      }
      navigate(orderId ? `/orders/${orderId}` : "/orders", { replace: true });
    } catch (e) {
      toast.push({ variant: "error", title: "Checkout failed", message: getErrorMessage(e, "Failed to place order.") });
    } finally {
      setIsSubmitting(false);
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
          <Button asChild className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
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
          <Button asChild className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/me/addresses">Go to address book</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Checkout</div>
            <div className="text-3xl font-semibold tracking-tight">Place your order</div>
            <div className="mt-1 text-sm text-muted-foreground">Confirm address, extras, and create the order.</div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur">
            <Link to="/cart">Back to cart</Link>
          </Button>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="space-y-4">
          <Card className="overflow-hidden bg-background/70 backdrop-blur">
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
                        "pressable rounded-2xl border p-3 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md",
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
              <Button asChild variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur">
                <Link to="/me/addresses">Manage addresses</Link>
              </Button>
            </CardContent>
          </Card>

          <Card className="bg-background/70 backdrop-blur">
            <CardHeader>
              <CardTitle>Extras</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Shipping fee</div>
                <Input className="rounded-xl bg-background/70 backdrop-blur" value={shippingFee} onChange={(e) => setShippingFee(e.target.value)} inputMode="numeric" />
              </div>
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Voucher</div>
                <div className="flex flex-wrap items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    className="rounded-xl bg-background/70 backdrop-blur"
                    onClick={() => {
                      setIsVoucherPickerOpen(true);
                      setVoucherView("eligible");
                      setSearchCode("");
                      setSearchResults([]);
                      setSearchError(null);
                      void loadEligibleVouchers(0);
                    }}
                  >
                    {appliedVoucher?.id ? "Change voucher" : "Apply voucher"}
                  </Button>
                  {appliedVoucher?.id ? (
                    <Button
                      type="button"
                      variant="outline"
                      className="rounded-xl bg-background/70 text-rose-600 hover:bg-rose-500/10 hover:text-rose-700 backdrop-blur"
                      onClick={() => {
                        setAppliedVoucher(null);
                        void refreshDiscount(null);
                      }}
                    >
                      Remove
                    </Button>
                  ) : null}
                  <Button asChild variant="outline" className="rounded-xl bg-background/70 backdrop-blur">
                    <Link to="/me/vouchers">My vouchers</Link>
                  </Button>
                </div>
                {appliedVoucher?.id ? (
                  <div className="rounded-xl border border-emerald-500/20 bg-emerald-500/10 px-3 py-2 text-xs text-emerald-700 backdrop-blur">
                    Applied: <span className="font-medium">{appliedVoucher.code}</span> — {appliedVoucher.name || "Voucher"}
                  </div>
                ) : (
                  <div className="text-xs text-muted-foreground">Pick a voucher from the eligible list, or search by code.</div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="sticky top-24 overflow-hidden bg-background/70 backdrop-blur shadow-sm">
          <CardHeader className="relative">
            <CardTitle>Order summary</CardTitle>
          </CardHeader>
          <CardContent className="relative space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Subtotal</span>
              <span>{money(subtotal, currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Shipping</span>
              <span>{money(shipping, currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Discount</span>
              <span className={discount > 0 ? "text-emerald-600" : ""}>- {money(discount, currency)}</span>
            </div>
            {discountStatus === "loading" ? <div className="text-xs text-muted-foreground">Calculating discount…</div> : null}
            {discountStatus === "error" && discountError ? <div className="text-xs text-rose-600">{discountError}</div> : null}
            <div className="h-px bg-border" />
            <div className="flex items-center justify-between text-base font-semibold">
              <span>Total</span>
              <span>{money(total, currency)}</span>
            </div>

            <Button disabled={!canSubmit} onClick={placeOrder} className="h-10 w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
              {isSubmitting ? "Placing..." : "Place order"}
            </Button>
          </CardContent>
        </Card>
      </div>

      {isVoucherPickerOpen ? (
        <div
          className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 p-4 backdrop-blur-sm sm:items-center"
          role="dialog"
          aria-modal="true"
          onClick={(e) => {
            if (e.target === e.currentTarget) setIsVoucherPickerOpen(false);
          }}
        >
          <div className="flex w-full max-w-3xl flex-col overflow-hidden rounded-3xl border bg-background/90 shadow-xl backdrop-blur max-h-[85vh]">
            <div className="flex items-center justify-between gap-3 border-b p-4">
              <div>
                <div className="text-sm text-muted-foreground">Checkout</div>
                <div className="text-lg font-semibold">Select a voucher</div>
              </div>
              <div className="flex items-center gap-2">
                {voucherView === "search" ? (
                  <Button
                    variant="outline"
                    className="h-9 rounded-xl bg-background/70 backdrop-blur"
                    onClick={() => {
                      void resetVoucherSearch();
                    }}
                  >
                    Reset
                  </Button>
                ) : null}
                <Button variant="outline" className="h-9 rounded-xl bg-background/70 backdrop-blur" onClick={() => setIsVoucherPickerOpen(false)}>
                  Close
                </Button>
              </div>
            </div>

            <div className="space-y-4 p-4 overflow-auto">
              <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-center">
                <div className="space-y-1">
                  <div className="text-sm font-medium">Search by code</div>
                  <div className="text-xs text-muted-foreground">Search overrides the eligible list until you reset.</div>
                </div>
                <div className="flex gap-2">
                  <Input
                    className="h-10 w-56 rounded-xl bg-background/70 backdrop-blur"
                    value={searchCode}
                    onChange={(e) => {
                      setSearchCode(e.target.value);
                      setSearchResults([]);
                      setSearchError(null);
                    }}
                    placeholder="e.g. SAVE10"
                  />
                  <Button className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={runVoucherSearch} disabled={!searchCode.trim() || searchLoading}>
                    {searchLoading ? "Searching..." : "Search"}
                  </Button>
                </div>
              </div>

              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <div className="text-sm font-medium">{voucherView === "search" ? "Search results" : "Eligible vouchers"}</div>
                  {voucherView === "eligible" ? (
                    <div className="text-xs text-muted-foreground">Based on current order total: {money(originTotal, currency)}</div>
                  ) : (
                    <div className="text-xs text-muted-foreground">
                      Code: <span className="font-medium text-foreground">{searchCode.trim() || "-"}</span>
                    </div>
                  )}
                </div>
                {voucherView === "eligible" ? (
                  <div className="flex items-center gap-2">
                    <Button
                      variant="outline"
                      className="h-9 rounded-xl"
                      disabled={eligibleLoading || eligiblePage <= 0}
                      onClick={() => void loadEligibleVouchers(Math.max(0, eligiblePage - 1))}
                    >
                      Prev
                    </Button>
                    <Button
                      variant="outline"
                      className="h-9 rounded-xl"
                      disabled={eligibleLoading || eligiblePage + 1 >= eligibleTotalPages}
                      onClick={() => void loadEligibleVouchers(Math.min(eligibleTotalPages - 1, eligiblePage + 1))}
                    >
                      Next
                    </Button>
                  </div>
                ) : null}
              </div>

              {voucherView === "search" && searchError ? (
                <div className="rounded-2xl border border-rose-500/20 bg-rose-500/10 p-3 text-sm text-rose-700">{searchError}</div>
              ) : null}
              {voucherView === "eligible" && eligibleError ? (
                <div className="rounded-2xl border border-rose-500/20 bg-rose-500/10 p-3 text-sm text-rose-700">{eligibleError}</div>
              ) : null}

              {voucherView === "eligible" && eligibleLoading ? (
                <div className="space-y-2">
                  <div className="h-20 animate-pulse rounded-2xl border bg-muted" />
                  <div className="h-20 animate-pulse rounded-2xl border bg-muted" />
                </div>
              ) : voucherView === "search" && searchLoading ? (
                <div className="space-y-2">
                  <div className="h-20 animate-pulse rounded-2xl border bg-muted" />
                  <div className="h-20 animate-pulse rounded-2xl border bg-muted" />
                </div>
              ) : (voucherView === "search" ? searchResults : eligibleItems).length === 0 ? (
                <div className="rounded-2xl border bg-background/60 p-3 text-sm text-muted-foreground">
                  {voucherView === "search" ? "No vouchers found for this code." : "No eligible vouchers for this order total."}
                </div>
              ) : (
                <div className="space-y-2 max-h-[55vh] overflow-auto pr-1">
                  {(voucherView === "search" ? searchResults : eligibleItems).map((v) => {
                    const id = Number(v.id ?? 0);
                    const remaining = typeof v.activeUsesForUser === "number" ? v.activeUsesForUser : undefined;
                    const disabled = remaining !== undefined && remaining <= 0;
                    return (
                      <button
                        key={String(id || v.code)}
                        type="button"
                        disabled={disabled}
                        onClick={() => {
                          if (disabled) return;
                          setAppliedVoucher(v);
                          void refreshDiscount(Number(v.id ?? 0));
                          setIsVoucherPickerOpen(false);
                          toast.push({ variant: "success", title: "Voucher selected", message: v.code ? `Applied ${v.code}.` : "Voucher applied." });
                        }}
                        className={[
                          "w-full rounded-2xl border bg-background/60 p-3 text-left shadow-sm transition hover:-translate-y-0.5 hover:shadow-md",
                          disabled ? "opacity-60" : "hover:bg-muted",
                          appliedVoucher?.id && v.id && Number(appliedVoucher.id) === Number(v.id) ? "border-primary ring-1 ring-primary/20" : "",
                        ].join(" ")}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <div className="font-medium">{v.name || v.code || "Voucher"}</div>
                            <div className="mt-1 text-xs text-muted-foreground">
                              {v.code ? `Code: ${v.code}` : ""} {v.discountType ? `• ${voucherDiscountLabel(v)}` : ""}
                            </div>
                          </div>
                          <div className="shrink-0 text-right">
                            <div className="text-xs text-muted-foreground">Remaining</div>
                            <div className="mt-1 rounded-full bg-primary/10 px-2 py-1 text-xs ring-1 ring-primary/20">{remaining ?? "-"}</div>
                          </div>
                        </div>
                        {v.minOrderTotal !== undefined && v.minOrderTotal !== null ? (
                          <div className="mt-2 text-xs text-muted-foreground">Min order: {formatCurrency(toNumber(v.minOrderTotal), currency)}</div>
                        ) : null}
                      </button>
                    );
                  })}
                </div>
              )}
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
