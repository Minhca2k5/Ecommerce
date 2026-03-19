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
import { ApiError } from "@/lib/apiError";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { getOrCreateCart, getOrCreateGuestCart, getStoredGuestId, type CartResponse } from "@/lib/cartApi";
import { createGuestOrder, createMyOrder, getMyVoucherDiscount } from "@/lib/orderApi";
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

function makeGuestAddressSnapshotId(seed: string) {
  let hash = 0;
  for (let i = 0; i < seed.length; i += 1) {
    hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
  }
  return (hash % 900000000) + 100000000;
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
  const [guestAddress, setGuestAddress] = useState({
    fullName: "",
    phone: "",
    line1: "",
    city: "",
    country: "Vietnam",
  });
  const [shippingFee, setShippingFee] = useState<string>("0");
  const [selectedCurrency, setSelectedCurrency] = useState<string>("VND");

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
  const isGuest = !auth.isAuthenticated;

  const currency = selectedCurrency || cart?.currency || "VND";
  const subtotal = Number(cart?.itemsSubtotal ?? cart?.totalAmount ?? 0);
  const shipping = Math.max(0, Number(shippingFee || "0"));
  const originTotal = subtotal + shipping;
  const discount = Math.max(0, Number(discountAmount || 0));
  const taxRate = useMemo(() => ((currency || "VND").toUpperCase() === "JPY" ? 0.1 : 0.08), [currency]);
  const taxable = Math.max(0, originTotal - discount);
  const tax = taxable * taxRate;
  const total = taxable + tax;
  const guestAddressSnapshotId = useMemo(() => {
    const seed = [
      guestAddress.fullName.trim(),
      guestAddress.phone.trim(),
      guestAddress.line1.trim(),
      guestAddress.city.trim(),
      guestAddress.country.trim(),
    ].join("|");
    if (!seed.replace(/\|/g, "").trim()) return 0;
    return makeGuestAddressSnapshotId(seed);
  }, [guestAddress.city, guestAddress.country, guestAddress.fullName, guestAddress.line1, guestAddress.phone]);

  const canSubmit = useMemo(() => {
    if (isSubmitting) return false;
    if (!cart?.id) return false;
    if (!cart.items?.length) return false;
    if (isGuest) {
      return Boolean(
        guestAddress.fullName.trim() &&
          guestAddress.phone.trim() &&
          guestAddress.line1.trim() &&
          guestAddress.city.trim() &&
          guestAddressSnapshotId > 0,
      );
    }
    if (!selectedAddressId) return false;
    return true;
  }, [
    cart?.id,
    cart?.items?.length,
    guestAddress.city,
    guestAddress.fullName,
    guestAddress.line1,
    guestAddress.phone,
    guestAddressSnapshotId,
    isGuest,
    isSubmitting,
    selectedAddressId,
  ]);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);

    const loadCheckout = auth.isAuthenticated
      ? Promise.all([
          getOrCreateCart(),
          listAddresses().catch(() => [] as AddressResponse[]),
          getDefaultAddress().catch(() => null as AddressResponse | null),
        ])
      : Promise.all([getOrCreateGuestCart(), Promise.resolve([] as AddressResponse[]), Promise.resolve(null as AddressResponse | null)]);

    loadCheckout
      .then(([c, list, def]) => {
        if (!alive) return;
        setCart(c);
        setSelectedCurrency((c.currency || "VND").toUpperCase());
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
    if (isGuest) {
      setDiscountAmount(0);
      setDiscountStatus("idle");
      setDiscountError(null);
      return;
    }
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
  }, [cart?.id, isGuest, selectedAddressId, shippingFee, appliedVoucher?.id]);

  async function placeOrder() {
    if (!cart?.id) return;
    const addressIdSnapshot = isGuest ? guestAddressSnapshotId : Number(selectedAddressId);
    if (!addressIdSnapshot) return;
    setIsSubmitting(true);
    try {
      const itemNames = (cart?.items ?? [])
        .map((it: any) => String(it?.productName ?? it?.name ?? it?.title ?? "").trim())
        .filter(Boolean);
      const preview = itemNames.slice(0, 3).join(", ");
      const suffix = itemNames.length > 3 ? ", ..." : "";

      const voucherId = !isGuest && appliedVoucher?.id ? Number(appliedVoucher.id) : undefined;
      const idempotencyKey =
        typeof crypto !== "undefined" && "randomUUID" in crypto ? crypto.randomUUID() : `order_${Date.now()}`;
      const request = {
        cartId: Number(cart.id),
        addressIdSnapshot,
        ...(voucherId ? { voucherId } : {}),
        shippingFee: shipping,
        currency,
        status: "PENDING",
      };
      const guestId = cart.guestId || getStoredGuestId() || "";
      if (isGuest && !guestId) throw new Error("Missing guest checkout id");
      const created = isGuest
        ? await createGuestOrder(guestId, request, idempotencyKey)
        : await createMyOrder(request, idempotencyKey);

      toast.push({ variant: "success", title: "Order placed", message: "Your order has been created." });

      const orderId = Number(created.id ?? 0);
      if (orderId) {
        notifications.push({
          type: "ORDER",
          title: "Order created",
          message: `Items: ${preview || "Your cart"}${suffix} - Total: ${formatCurrency(Number(created.totalAmount ?? 0), created.currency || currency)} - Tap to view details.`,
          referenceId: orderId,
          referenceType: "ORDER",
        });
      }
      if (isGuest) {
        const guestToken = String(created.guestAccessToken || "");
        toast.push({
          variant: "default",
          title: "Guest order created",
          message: `Order id: ${orderId || "N/A"}. A secure guest tracking link is ready.`,
        });
        if (orderId && guestToken) {
          const q = new URLSearchParams({ token: guestToken }).toString();
          navigate(`/guest/orders/${orderId}?${q}`, { replace: true });
        } else {
          navigate("/products", { replace: true });
        }
      } else {
        navigate(orderId ? `/orders/${orderId}` : "/orders", { replace: true });
      }
    } catch (e) {
      if (e instanceof ApiError && e.status === 429) {
        toast.push({
          variant: "error",
          title: "Too many attempts",
          message: "You are submitting too quickly. Please wait a bit and try again.",
        });
      } else {
        toast.push({ variant: "error", title: "Checkout failed", message: getErrorMessage(e, "Failed to place order.") });
      }
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
        title="Could not load checkout"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-md bg-primary text-primary-foreground">
            Try again
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
          <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
            <Link to="/products">Browse all products</Link>
          </Button>
        }
      />
    );
  }

  if (!isGuest && !addresses.length) {
    return (
      <EmptyState
        title="Add an address first"
        description="Checkout requires at least one delivery address."
        action={
          <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
            <Link to="/me/addresses">Manage addresses</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Checkout</div>
            <div className="text-2xl font-semibold">Review and place order</div>
            <div className="mt-1 text-sm text-muted-foreground">Confirm address, vouchers, and totals.</div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-md bg-background">
            <Link to="/cart">Back to cart</Link>
          </Button>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <div className="space-y-4">
          <Card className="overflow-hidden bg-background">
            <CardHeader className="relative">
              <CardTitle>{isGuest ? "Guest shipping info" : "Delivery address"}</CardTitle>
            </CardHeader>
            <CardContent className="relative space-y-3">
              {isGuest ? (
                <div className="space-y-2">
                  <div className="text-xs text-muted-foreground">Enter shipping info to continue.</div>
                  <Input
                    className="rounded-md bg-white"
                    value={guestAddress.fullName}
                    onChange={(e) => setGuestAddress((s) => ({ ...s, fullName: e.target.value }))}
                    placeholder="Full name"
                  />
                  <Input
                    className="rounded-md bg-white"
                    value={guestAddress.phone}
                    onChange={(e) => setGuestAddress((s) => ({ ...s, phone: e.target.value }))}
                    placeholder="Phone number"
                  />
                  <Input
                    className="rounded-md bg-white"
                    value={guestAddress.line1}
                    onChange={(e) => setGuestAddress((s) => ({ ...s, line1: e.target.value }))}
                    placeholder="Address line"
                  />
                  <div className="grid gap-2 sm:grid-cols-2">
                    <Input
                      className="rounded-md bg-white"
                      value={guestAddress.city}
                      onChange={(e) => setGuestAddress((s) => ({ ...s, city: e.target.value }))}
                      placeholder="City"
                    />
                    <Input
                      className="rounded-md bg-white"
                      value={guestAddress.country}
                      onChange={(e) => setGuestAddress((s) => ({ ...s, country: e.target.value }))}
                      placeholder="Country"
                    />
                  </div>
                  <div className="rounded-md border bg-background px-3 py-2 text-xs text-muted-foreground">
                    Snapshot id (auto): <span className="font-medium text-foreground">{guestAddressSnapshotId || "-"}</span>
                  </div>
                </div>
              ) : (
                <>
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
                            "pressable rounded-md border p-3 text-left shadow-sm transition hover:shadow-md",
                            active ? "border-primary bg-primary/10 ring-1 ring-primary/20" : "bg-background hover:bg-muted",
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
                  <Button asChild variant="outline" className="h-10 rounded-md bg-background">
                    <Link to="/me/addresses">Manage addresses</Link>
                  </Button>
                </>
              )}
            </CardContent>
          </Card>

          <Card className="bg-background">
            <CardHeader>
              <CardTitle>Extras</CardTitle>
            </CardHeader>
            <CardContent className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Shipping fee</div>
                <Input className="rounded-md bg-white" value={shippingFee} onChange={(e) => setShippingFee(e.target.value)} inputMode="numeric" />
              </div>
              <div className="space-y-2">
                <div className="text-xs font-medium text-muted-foreground">Currency</div>
                <select title="Select option" className="h-10 w-full rounded-md border bg-white px-3 text-sm"
                  value={currency}
                  onChange={(e) => setSelectedCurrency(e.target.value)}
                >
                  <option value="VND">VND</option>
                  <option value="USD">USD</option>
                  <option value="JPY">JPY</option>
                </select>
              </div>
              <div className="space-y-2 sm:col-span-2">
                <div className="text-xs font-medium text-muted-foreground">Voucher</div>
                {isGuest ? (
                  <div className="text-xs text-muted-foreground">Vouchers are available for signed-in checkout.</div>
                ) : (
                <div className="flex flex-wrap items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    className="rounded-md bg-background"
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
                      className="rounded-md bg-background action-danger"
                      onClick={() => {
                        setAppliedVoucher(null);
                        void refreshDiscount(null);
                      }}
                    >
                      Remove
                    </Button>
                  ) : null}
                  <Button asChild variant="outline" className="rounded-md bg-background">
                    <Link to="/me/vouchers">Your vouchers</Link>
                  </Button>
                </div>
                )}
                {!isGuest && appliedVoucher?.id ? (
                  <div className="rounded-md border border-emerald-500/20 bg-emerald-500/10 px-3 py-2 text-xs text-emerald-700">
                    Applied: <span className="font-medium">{appliedVoucher.code}</span> - {appliedVoucher.name || "Voucher"}
                  </div>
                ) : !isGuest ? (
                  <div className="text-xs text-muted-foreground">Pick an eligible voucher or search by code.</div>
                ) : null}
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="sticky top-24 overflow-hidden bg-background shadow-sm">
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
              <span className={discount > 0 ? "text-success" : ""}>- {money(discount, currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Tax</span>
              <span>{money(tax, currency)}</span>
            </div>
            {discountStatus === "loading" ? <div className="text-xs text-muted-foreground">Calculating discount...</div> : null}
            {discountStatus === "error" && discountError ? <div className="text-xs text-danger">{discountError}</div> : null}
            <div className="h-px bg-border" />
            <div className="flex items-center justify-between text-base font-semibold">
              <span>Total</span>
              <span>{money(total, currency)}</span>
            </div>

            <Button disabled={!canSubmit} onClick={placeOrder} className="h-10 w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90">
              {isSubmitting ? "Placing..." : "Place order"}
            </Button>
          </CardContent>
        </Card>
      </div>

      {isVoucherPickerOpen ? (
        <div
          className="fixed inset-0 z-50 flex items-end justify-center bg-black/40 p-4-sm sm:items-center"
          role="dialog"
          aria-modal="true"
          onClick={(e) => {
            if (e.target === e.currentTarget) setIsVoucherPickerOpen(false);
          }}
        >
          <div className="flex w-full max-w-3xl flex-col overflow-hidden rounded-md border bg-background shadow-md max-h-[85vh]">
            <div className="flex items-center justify-between gap-3 border-b p-4">
              <div>
                <div className="text-sm text-muted-foreground">Checkout</div>
                <div className="text-lg font-semibold">Select a voucher</div>
              </div>
              <div className="flex items-center gap-2">
                {voucherView === "search" ? (
                  <Button
                    variant="outline"
                    className="h-9 rounded-md bg-background"
                    onClick={() => {
                      void resetVoucherSearch();
                    }}
                  >
                    Reset
                  </Button>
                ) : null}
                <Button variant="outline" className="h-9 rounded-md bg-background" onClick={() => setIsVoucherPickerOpen(false)}>
                  Close
                </Button>
              </div>
            </div>

            <div className="space-y-4 p-4 overflow-auto">
              <div className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-center">
                <div className="space-y-1">
                  <div className="text-sm font-medium">Search by code</div>
                  <div className="text-xs text-muted-foreground">Search replaces the eligible list until reset.</div>
                </div>
                <div className="flex gap-2">
                  <Input
                    className="h-10 w-56 rounded-md bg-white"
                    value={searchCode}
                    onChange={(e) => {
                      setSearchCode(e.target.value);
                      setSearchResults([]);
                      setSearchError(null);
                    }}
                    placeholder="e.g. SAVE10"
                  />
                  <Button className="h-10 rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={runVoucherSearch} disabled={!searchCode.trim() || searchLoading}>
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
                      className="h-9 rounded-md"
                      disabled={eligibleLoading || eligiblePage <= 0}
                      onClick={() => void loadEligibleVouchers(Math.max(0, eligiblePage - 1))}
                    >
                      Prev
                    </Button>
                    <Button
                      variant="outline"
                      className="h-9 rounded-md"
                      disabled={eligibleLoading || eligiblePage + 1 >= eligibleTotalPages}
                      onClick={() => void loadEligibleVouchers(Math.min(eligibleTotalPages - 1, eligiblePage + 1))}
                    >
                      Next
                    </Button>
                  </div>
                ) : null}
              </div>

              {voucherView === "search" && searchError ? (
                <div className="rounded-md border border-rose-500/20 bg-rose-500/10 p-3 text-sm text-rose-700">{searchError}</div>
              ) : null}
              {voucherView === "eligible" && eligibleError ? (
                <div className="rounded-md border border-rose-500/20 bg-rose-500/10 p-3 text-sm text-rose-700">{eligibleError}</div>
              ) : null}

              {voucherView === "eligible" && eligibleLoading ? (
                <div className="space-y-2">
                  <div className="h-20 animate-pulse rounded-md border bg-muted" />
                  <div className="h-20 animate-pulse rounded-md border bg-muted" />
                </div>
              ) : voucherView === "search" && searchLoading ? (
                <div className="space-y-2">
                  <div className="h-20 animate-pulse rounded-md border bg-muted" />
                  <div className="h-20 animate-pulse rounded-md border bg-muted" />
                </div>
              ) : (voucherView === "search" ? searchResults : eligibleItems).length === 0 ? (
                <div className="rounded-md border bg-background p-3 text-sm text-muted-foreground">
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
                          "w-full rounded-md border bg-background p-3 text-left shadow-sm transition hover:shadow-md",
                          disabled ? "opacity-60" : "hover:bg-muted",
                          appliedVoucher?.id && v.id && Number(appliedVoucher.id) === Number(v.id) ? "border-primary ring-1 ring-primary/20" : "",
                        ].join(" ")}
                      >
                        <div className="flex items-start justify-between gap-3">
                          <div>
                            <div className="font-medium">{v.name || v.code || "Voucher"}</div>
                            <div className="mt-1 text-xs text-muted-foreground">
                              {v.code ? `Code: ${v.code}` : ""} {v.discountType ? `- ${voucherDiscountLabel(v)}` : ""}
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

