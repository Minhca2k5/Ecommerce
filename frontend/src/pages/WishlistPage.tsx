import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import SafeImage from "@/components/SafeImage";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/app/ToastProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { clearWishlist, listMyWishlists, removeWishlist, type WishlistResponse } from "@/lib/wishlistApi";

function money(value: number | string | undefined, currency: string | undefined) {
  const n = typeof value === "string" ? Number(value) : value ?? 0;
  return formatCurrency(Number.isFinite(n) ? n : 0, currency || "VND");
}

export default function WishlistPage() {
  const toast = useToast();
  const notifications = useNotifications();
  const [items, setItems] = useState<WishlistResponse[]>([]);
  const [search, setSearch] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<WishlistResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isClearOpen, setIsClearOpen] = useState(false);
  const [isClearing, setIsClearing] = useState(false);

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return items;
    return items.filter((w) => String(w.productName || "").toLowerCase().includes(q));
  }, [items, search]);

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const page = await listMyWishlists({ page: 0, size: 50 });
      setItems(page.content ?? []);
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load wishlist."));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  async function onRemove() {
    const id = Number(deleteTarget?.id ?? 0);
    if (!id) return;
    setIsDeleting(true);
    try {
      await removeWishlist(id);
      toast.push({ variant: "success", title: "Removed", message: "Removed from wishlist." });
      notifications.push({
        type: "PRODUCT",
        title: "Wishlist updated",
        message: `Removed ${deleteTarget?.productName || "a product"} from wishlist.`,
        referenceId: Number(deleteTarget?.productId ?? 0) || undefined,
        referenceType: "PRODUCT",
      });
      setDeleteTarget(null);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Remove failed", message: getErrorMessage(e, "Failed to remove wishlist item.") });
    } finally {
      setIsDeleting(false);
    }
  }

  async function onClear() {
    setIsClearing(true);
    try {
      await clearWishlist();
      toast.push({ variant: "success", title: "Cleared", message: "Wishlist cleared." });
      setIsClearOpen(false);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Clear failed", message: getErrorMessage(e, "Failed to clear wishlist.") });
    } finally {
      setIsClearing(false);
    }
  }

  if (isLoading) return <div className="space-y-4"><LoadingCard /><LoadingCard /></div>;
  if (error) return <EmptyState title="Couldn't load wishlist" description={error} action={<Button onClick={refresh} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">Retry</Button>} />;
  if (!items.length) return <EmptyState title="Wishlist is empty" description="Save products you love for later." action={<Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white"><Link to="/products">Browse products</Link></Button>} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Account</div>
          <div className="text-3xl font-semibold tracking-tight">Wishlist</div>
          <div className="mt-1 text-sm text-muted-foreground">Your saved products.</div>
        </div>
        <div className="flex gap-2">
          <Button variant="outline" className="rounded-xl" onClick={() => setIsClearOpen(true)}>
            Clear all
          </Button>
          <Button asChild className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/products">Shop</Link>
          </Button>
        </div>
      </div>

      <Card className="shine">
        <CardHeader>
          <CardTitle className="text-base">Search</CardTitle>
        </CardHeader>
        <CardContent>
          <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search wishlist..." className="rounded-xl" />
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filtered.map((w) => (
          <Card key={String(w.id)} className="pressable overflow-hidden">
            <div className="relative aspect-[4/3] overflow-hidden bg-gradient-to-br from-primary/15 via-background to-background">
              <SafeImage
                src={w.url || ""}
                alt={w.productName || "Product"}
                fallbackKey={String(w.id ?? w.productId ?? "wish")}
                className="h-full w-full object-cover"
              />
            </div>
            <CardContent className="space-y-2 p-4">
              <div className="font-semibold line-clamp-2">{w.productName || "Product"}</div>
              <div className="text-sm text-primary font-bold">{money(w.productPrice, w.productCurrency)}</div>
              <div className="flex gap-2">
                <Button asChild variant="outline" className="w-full rounded-xl">
                  <Link to={`/products/${w.productId ?? ""}`}>View</Link>
                </Button>
                <Button variant="outline" className="w-full rounded-xl border-rose-500/20 text-rose-700 hover:bg-rose-500/10" onClick={() => setDeleteTarget(w)}>
                  Remove
                </Button>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Remove from wishlist?"
        description="This item will be removed from your wishlist."
        confirmText="Remove"
        variant="danger"
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={onRemove}
      />
      <ConfirmDialog
        isOpen={isClearOpen}
        title="Clear wishlist?"
        description="This will remove all saved products."
        confirmText="Clear"
        variant="danger"
        isLoading={isClearing}
        onClose={() => setIsClearOpen(false)}
        onConfirm={onClear}
      />
    </div>
  );
}
