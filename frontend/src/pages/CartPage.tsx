import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import SafeImage from "@/components/SafeImage";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { money } from "@/lib/format";
import {
  addOrUpdateCartItem,
  addOrUpdateGuestCartItem,
  clearCart,
  clearGuestCart,
  deleteCartItem,
  deleteGuestCartItem,
  getOrCreateCart,
  getOrCreateGuestCart,
  getStoredGuestId,
  listGuestCartItems,
  listMyCartItems,
  returnUpdateCartItem,
  type CartItemResponse,
  type CartResponse,
} from "@/lib/cartApi";

export default function CartPage() {
  const auth = useAuth();
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
  const isGuest = !auth.isAuthenticated;

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
      if (isGuest) {
        const c = await getOrCreateGuestCart();
        setCart(c);
        const guestId = c.guestId ?? getStoredGuestId();
        if (guestId) {
          const list = await listGuestCartItems(guestId);
          setItems(list ?? []);
        } else {
          setItems([]);
        }
      } else {
        const c = await getOrCreateCart();
        setCart(c);
        const id = Number(c.id ?? 0);
        if (id) {
          const page = await listMyCartItems(id, { page: 0, size: 50, productName: search.trim() || undefined });
          setItems(page.content ?? []);
        } else {
          setItems([]);
        }
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
      if (isGuest) {
        const guestId = cart?.guestId ?? getStoredGuestId();
        if (!guestId) throw new Error("Missing guestId");
        if (desiredQty <= 0) {
          if (item.id) await deleteGuestCartItem(guestId, Number(item.id));
        } else {
          if (item.id) await deleteGuestCartItem(guestId, Number(item.id));
          await addOrUpdateGuestCartItem(guestId, productId, desiredQty);
        }
      } else {
        if (delta > 0) await addOrUpdateCartItem({ cartId, productId, quantity: delta });
        else await returnUpdateCartItem({ cartId, productId, quantity: Math.abs(delta) });
      }
      await refresh();
      window.dispatchEvent(new CustomEvent("cart:changed"));
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
      if (isGuest) {
        const guestId = cart?.guestId ?? getStoredGuestId();
        if (!guestId) throw new Error("Missing guestId");
        if (desiredQty <= 0) {
          if (item.id) await deleteGuestCartItem(guestId, Number(item.id));
        } else {
          if (item.id) await deleteGuestCartItem(guestId, Number(item.id));
          await addOrUpdateGuestCartItem(guestId, productId, desiredQty);
        }
      } else {
        if (appliedDelta > 0) await addOrUpdateCartItem({ cartId, productId, quantity: appliedDelta });
        else await returnUpdateCartItem({ cartId, productId, quantity: Math.abs(appliedDelta) });
      }
      await refresh();
      window.dispatchEvent(new CustomEvent("cart:changed"));
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update quantity.") });
    }
  }

  async function removeItem() {
    if (!cartId || !deleteTarget?.id) return;
    setIsDeleting(true);
    try {
      if (isGuest) {
        const guestId = cart?.guestId ?? getStoredGuestId();
        if (!guestId) throw new Error("Missing guestId");
        await deleteGuestCartItem(guestId, Number(deleteTarget.id));
      } else {
        await deleteCartItem(cartId, Number(deleteTarget.id));
      }
      toast.push({ variant: "success", title: "Removed", message: "Item removed from cart." });
      setDeleteTarget(null);
      await refresh();
      window.dispatchEvent(new CustomEvent("cart:changed"));
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
      if (isGuest) {
        const guestId = cart?.guestId ?? getStoredGuestId();
        if (!guestId) throw new Error("Missing guestId");
        await clearGuestCart(guestId);
      } else {
        await clearCart(cartId);
      }
      toast.push({ variant: "success", title: "Cleared", message: "Cart cleared." });
      setIsClearOpen(false);
      await refresh();
      window.dispatchEvent(new CustomEvent("cart:changed"));
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
        title="Could not load cart"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-md bg-primary text-primary-foreground">
            Try again
          </Button>
        }
      />
    );
  }

  if (!items.length) {
    return (
      <EmptyState
        title="Your cart is empty"
        description="Add a few items to get started."
        action={
          <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
            <Link to="/products">Continue shopping</Link>
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="text-2xl font-semibold">Cart</div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-md bg-background" onClick={() => setIsClearOpen(true)}>
              Clear cart
            </Button>
            <Button
              className="h-10 rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={() => navigate("/checkout")}
            >
              {isGuest ? "Checkout as guest" : "Checkout"}
            </Button>
          </div>
        </div>
      </section>

      <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
        <Card className="h-fit bg-background">
          <CardHeader>
            <CardTitle className="text-base">Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex gap-2">
              <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search items" className="rounded-md" />
              <Button variant="outline" className="rounded-md bg-background" onClick={onSearch} disabled={!cartId}>
                Search
              </Button>
            </div>

            <div className="space-y-3">
              {items.map((item) => (
                <div key={String(item.id ?? item.productId)} className="pressable group flex gap-3 rounded-md border bg-background p-3 shadow-sm transition hover:shadow-md">
                  <div className="h-20 w-20 overflow-hidden rounded-md border bg-muted">
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
                        <Button type="button" variant="outline" className="h-9 w-9 rounded-md bg-background px-0" onClick={() => bumpQty(item, -1)}>
                          -
                        </Button>
                        <input
                          aria-label="Quantity"
                          title="Quantity"
                          value={String(item.quantity ?? 1)}
                          onChange={(e) => setQtyAbsolute(item, Number(e.target.value || "1"))}
                          className="h-9 w-14 rounded-md border bg-background text-center text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-primary/30"
                          inputMode="numeric"
                        />
                        <Button type="button" variant="outline" className="h-9 w-9 rounded-md bg-background px-0" onClick={() => bumpQty(item, 1)}>
                          +
                        </Button>
                      </div>

                      <div className="flex gap-2">
                        <Button asChild variant="outline" className="rounded-md bg-background">
                          <Link to={`/products/${item.productId ?? ""}`}>View details</Link>
                        </Button>
                        <Button variant="outline" className="rounded-md border-rose-500/20 bg-background text-rose-700 hover:bg-rose-500/10" onClick={() => setDeleteTarget(item)}>
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

        <Card className="sticky top-24 overflow-hidden bg-background shadow-sm">
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
            {summary.discount > 0 ? (
              <div className="rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs font-semibold text-emerald-800">
                You save {money(summary.discount, currency)} today
              </div>
            ) : null}
            <div className="h-px bg-border" />
            <div className="flex items-center justify-between text-base font-semibold">
              <span>Total</span>
              <span>{money(summary.total, currency)}</span>
            </div>
            <Button
              className="h-10 w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
              onClick={() => navigate("/checkout")}
            >
              {isGuest ? "Checkout as guest" : "Checkout"} - {money(summary.total, currency)}
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
