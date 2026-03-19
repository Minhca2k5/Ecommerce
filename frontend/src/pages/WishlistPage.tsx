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
  if (error) return <EmptyState title="Could not load wishlist" description={error} action={<Button onClick={refresh} className="h-10 rounded-md bg-primary text-primary-foreground">Try again</Button>} />;
  if (!items.length) return <EmptyState title="No saved items yet" description="Tap the heart on any product to save it here." action={<Button asChild className="h-10 rounded-md bg-primary text-primary-foreground"><Link to="/products">Browse all products</Link></Button>} />;

  return (
    <div className="space-y-8">
      <section className="page-hero">
        <div className="hero-orb hero-orb--a" />
        <div className="hero-orb hero-orb--b" />
        <div className="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="max-w-2xl space-y-2">
            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Wishlist</div>
            <div className="text-3xl font-semibold tracking-tight">Your saved items</div>
            <p className="text-sm text-muted-foreground">
              Save it now, buy it later.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button variant="outline" className="h-10 rounded-md bg-white" onClick={() => setIsClearOpen(true)}>
              Clear all
            </Button>
            <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
              <Link to="/products">Browse all products</Link>
            </Button>
          </div>
        </div>
      </section>

      <Card className="panel-card">
        <CardHeader>
          <CardTitle className="text-base">Search</CardTitle>
        </CardHeader>
        <CardContent>
          <Input value={search} onChange={(e) => setSearch(e.target.value)} placeholder="Search your wishlist" className="rounded-md bg-white" />
        </CardContent>
      </Card>

      <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
        {filtered.map((w) => (
          <Card key={String(w.id)} className="pressable group overflow-hidden bg-white/90">
            <div className="relative aspect-[4/3] overflow-hidden bg-muted/40">
              <SafeImage
                src={w.url || ""}
                alt={w.productName || "Product"}
                fallbackKey={String(w.id ?? w.productId ?? "wish")}
                className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
              />
              <div className="pointer-events-none absolute inset-0 bg-black/10" />
            </div>
            <CardContent className="space-y-2 p-4">
              <div className="font-semibold line-clamp-2">{w.productName || "Product"}</div>
              <div className="text-sm text-primary font-bold">{money(w.productPrice, w.productCurrency)}</div>
              <div className="flex gap-2">
                <Button asChild variant="outline" className="w-full rounded-md bg-white">
                  <Link to={`/products/${w.productId ?? ""}`}>View details</Link>
                </Button>
                <Button variant="outline" className="w-full rounded-md border-rose-500/20 bg-white text-rose-700 hover:bg-rose-500/10" onClick={() => setDeleteTarget(w)}>
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

