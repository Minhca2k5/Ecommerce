import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import SafeImage from "@/components/SafeImage";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import {
  addOrUpdateCartItem,
  clearCart,
  deleteCartItem,
  getOrCreateCart,
  listMyCartItems,
  returnUpdateCartItem,
  type CartItemResponse,
  type CartResponse,
} from "@/lib/cartApi";

function money(value: number | undefined, currency: string | undefined) {
  if (value === undefined || value === null) return "-";
  return formatCurrency(value, currency || "VND");
}

export default function CartPage() {
  const toast = useToast();
  const navigate = useNavigate();

  const [cart, setCart] = useState<CartResponse | null>(null);
  const [items, setItems] = useState<CartItemResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  const [isClearOpen, setIsClearOpen] = useState(false);
  const [isClearing, setIsClearing] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<CartItemResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const cartId = cart?.id ? Number(cart.id) : null;
  const currency = cart?.currency || items[0]?.productCurrency || "VND";

  const summary = useMemo(() => {
    const subtotal = Number(cart?.itemsSubtotal ?? 0);
    const shipping = Number(cart?.shippingFee ?? 0);
    const discount = Number(cart?.discount ?? 0);
    const total = Number(cart?.totalAmount ?? subtotal + shipping - discount);
    return { subtotal, shipping, discount, total };
  }, [cart?.discount, cart?.itemsSubtotal, cart?.shippingFee, cart?.totalAmount]);

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const c = await getOrCreateCart();
      setCart(c);
      const id = Number(c.id ?? 0);
      if (id) {
        const page = await listMyCartItems(id, { page: 0, size: 50, productName: search.trim() || undefined });
        setItems(page.content ?? []);
      } else {
        setItems([]);
      }
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load cart."));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function onSearch() {
    await refresh();
  }

  async function setQtyAbsolute(item: CartItemResponse, nextQty: number) {
    if (!cartId || !item.productId) return;
    const currentQty = Number(item.quantity ?? 1);
    const desiredQty = Math.max(1, Math.min(999, nextQty));
    const delta = desiredQty - currentQty;
    if (!delta) return;
    try {
      const productId = Number(item.productId);
      if (delta > 0) await addOrUpdateCartItem({ cartId, productId, quantity: delta });
      else await returnUpdateCartItem({ cartId, productId, quantity: Math.abs(delta) });
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update quantity.") });
    }
  }

  async function bumpQty(item: CartItemResponse, delta: number) {
    if (!cartId || !item.productId) return;
    const currentQty = Number(item.quantity ?? 1);
    const desiredQty = Math.max(1, Math.min(999, currentQty + delta));
    const appliedDelta = desiredQty - currentQty;
    if (!appliedDelta) return;
    try {
      const productId = Number(item.productId);
      if (appliedDelta > 0) await addOrUpdateCartItem({ cartId, productId, quantity: appliedDelta });
      else await returnUpdateCartItem({ cartId, productId, quantity: Math.abs(appliedDelta) });
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update quantity.") });
    }
  }

  async function removeItem() {
    if (!cartId || !deleteTarget?.id) return;
    setIsDeleting(true);
    try {
      await deleteCartItem(cartId, Number(deleteTarget.id));
      toast.push({ variant: "success", title: "Removed", message: "Item removed from cart." });
      setDeleteTarget(null);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Remove failed", message: getErrorMessage(e, "Failed to remove item.") });
    } finally {
      setIsDeleting(false);
    }
  }

  async function clearAll() {
    if (!cartId) return;
    setIsClearing(true);
    try {
      await clearCart(cartId);
      toast.push({ variant: "success", title: "Cleared", message: "Cart cleared." });
      setIsClearOpen(false);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Clear failed", message: getErrorMessage(e, "Failed to clear cart.") });
    } finally {
      setIsClearing(false);
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
        title="Couldn't load cart"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            Retry
          </Button>
        }
      />
    );
  }

  if (!items.length) {
    return (
      <EmptyState
        title="Your cart is empty"
        description="Add products you love, then come back to checkout."
        action={
          <Button asChild className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/products">Continue shopping</Link>
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
            <div className="text-3xl font-semibold tracking-tight">Your cart</div>
            <div className="mt-1 text-sm text-muted-foreground">Adjust quantities and proceed to checkout.</div>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur" onClick={() => setIsClearOpen(true)}>
              Clear cart
            </Button>
            <Button className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={() => navigate("/checkout")}>
              Checkout
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="h-fit shine bg-background/70 backdrop-blur">
          <CardHeader>
            <CardTitle className="text-base">Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search in cart..." className="rounded-xl" />
              <Button variant="outline" className="rounded-xl bg-background/70 backdrop-blur" onClick={onSearch} disabled={!cartId}>
                Search
              </Button>
            </div>

            <div className="space-y-3">
              {items.map((item) => (
                <div key={String(item.id ?? item.productId)} className="pressable group flex gap-3 rounded-2xl border bg-background/70 p-3 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-lg">
                  <div className="h-20 w-20 overflow-hidden rounded-xl border bg-muted">
                    <SafeImage
                      src={item.url ?? ""}
                      alt={item.productName || "Product"}
                      fallbackKey={String(item.id ?? item.productId ?? "cart")}
                      className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
                    />
                  </div>
                  <div className="min-w-0 flex-1">
                    <div className="flex items-start justify-between gap-2">
                      <div className="min-w-0">
                        <div className="truncate font-medium">{item.productName || "Product"}</div>
                        <div className="mt-1 text-xs text-muted-foreground">{money(Number(item.productPrice ?? item.unitPriceSnapshot ?? 0), currency)}</div>
                      </div>
                      <div className="text-right text-sm font-semibold">{money(Number(item.lineTotal ?? 0), currency)}</div>
                    </div>

                    <div className="mt-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                      <div className="inline-flex items-center gap-2">
                        <Button type="button" variant="outline" className="h-9 w-9 rounded-xl bg-background/70 px-0 backdrop-blur" onClick={() => bumpQty(item, -1)}>
                          -
                        </Button>
                        <input
                          value={String(item.quantity ?? 1)}
                          onChange={(e) => setQtyAbsolute(item, Number(e.target.value || "1"))}
                          className="h-9 w-14 rounded-xl border bg-background/70 text-center text-sm shadow-sm backdrop-blur focus:outline-none focus:ring-2 focus:ring-primary/30"
                          inputMode="numeric"
                        />
                        <Button type="button" variant="outline" className="h-9 w-9 rounded-xl bg-background/70 px-0 backdrop-blur" onClick={() => bumpQty(item, 1)}>
                          +
                        </Button>
                      </div>

                      <div className="flex gap-2">
                        <Button asChild variant="outline" className="rounded-xl bg-background/70 backdrop-blur">
                          <Link to={`/products/${item.productId ?? ""}`}>View</Link>
                        </Button>
                        <Button variant="outline" className="rounded-xl border-rose-500/20 bg-background/70 text-rose-700 hover:bg-rose-500/10 backdrop-blur" onClick={() => setDeleteTarget(item)}>
                          Remove
                        </Button>
                      </div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </CardContent>
        </Card>

        <Card className="sticky top-24 overflow-hidden bg-background/70 backdrop-blur shadow-sm">
          <CardHeader className="relative">
            <CardTitle>Summary</CardTitle>
          </CardHeader>
          <CardContent className="relative space-y-3">
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Subtotal</span>
              <span>{money(summary.subtotal, currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Shipping</span>
              <span>{money(summary.shipping, currency)}</span>
            </div>
            <div className="flex items-center justify-between text-sm">
              <span className="text-muted-foreground">Discount</span>
              <span>-{money(summary.discount, currency)}</span>
            </div>
            <div className="h-px bg-border" />
            <div className="flex items-center justify-between text-base font-semibold">
              <span>Total</span>
              <span>{money(summary.total, currency)}</span>
            </div>
            <Button className="h-10 w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={() => navigate("/checkout")}>
              Proceed to checkout
            </Button>
          </CardContent>
        </Card>
      </div>

      <ConfirmDialog
        isOpen={isClearOpen}
        title="Clear your cart?"
        description="This will remove all items from your cart."
        confirmText="Clear cart"
        variant="danger"
        isLoading={isClearing}
        onClose={() => setIsClearOpen(false)}
        onConfirm={clearAll}
      />
      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Remove item?"
        description="This item will be removed from your cart."
        confirmText="Remove"
        variant="danger"
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={removeItem}
      />
    </div>
  );
}
