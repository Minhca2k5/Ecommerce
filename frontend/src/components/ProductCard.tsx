import SafeImage from "@/components/SafeImage";
import RatingStars from "@/components/RatingStars";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { formatCurrency, formatCompactNumber } from "@/lib/format";
import { getBoolean, getNumber, getString, isRecord } from "@/lib/safe";
import { Link } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useCartActions } from "@/lib/useCartActions";

export default function ProductCard({
  product,
  href,
}: {
  product: unknown;
  href: string;
}) {
  const auth = useAuth();
  const { addToCart, isWorking } = useCartActions();
  const id = getNumber(product, "id") ?? 0;
  const name = getString(product, "name", "title") ?? `Product #${id}`;
  const price = getNumber(product, "salePrice", "price");
  const currency = getString(product, "currency");
  const rating = getNumber(product, "recentlyAverageRating", "rating");
  const reviewCount = getNumber(product, "reviewCount", "totalReviews");

  const inStockText =
    getString(product, "inStockStatus") ??
    (getBoolean(product, "inStock") === false ? "Out of stock" : undefined);

  const imageUrl =
    getString(product, "primaryImageUrl", "imageUrl", "thumbnailUrl") ??
    getString(product, "url") ??
    (isRecord(product) ? (product["primaryImage"] as string | undefined) : undefined);

  return (
    <Card className="shine pressable group overflow-hidden border-muted/60 bg-card/80 backdrop-blur shadow-sm transition hover:-translate-y-0.5 hover:shadow-xl">
      <div className="relative aspect-[4/3] overflow-hidden bg-gradient-to-br from-primary/15 via-background to-background">
        <SafeImage
          src={imageUrl}
          alt={name}
          fallbackKey={id || name}
          className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
        />
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/35 via-black/0 to-black/0 opacity-90" />

        <div className="absolute left-3 top-3 flex gap-2">
          {inStockText ? (
            <Badge variant="secondary" className="bg-background/70 backdrop-blur">
              {inStockText}
            </Badge>
          ) : null}
        </div>
      </div>

      <CardContent className="space-y-3 p-4">
        <div className="space-y-1">
          <div className="line-clamp-2 text-sm font-semibold leading-5">{name}</div>
          <div className="flex items-center justify-between gap-3">
            <div className="text-base font-bold text-primary">
              {formatCurrency(price, currency)}
            </div>
            {reviewCount && reviewCount > 0 ? (
              <div className="flex items-center gap-2">
                <RatingStars rating={rating} />
                <span className="text-xs text-muted-foreground">
                  ({formatCompactNumber(reviewCount)})
                </span>
              </div>
            ) : null}
          </div>
        </div>

        <div className="grid grid-cols-2 gap-2">
          <Button asChild variant="outline" className="w-full rounded-xl">
            <Link to={href}>View</Link>
          </Button>
          {auth.isAuthenticated ? (
            <Button
              className="w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
              onClick={() => addToCart(id, 1)}
              disabled={!id || isWorking}
            >
              Add
            </Button>
          ) : (
            <Button asChild className="w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
              <Link to="/login">Add</Link>
            </Button>
          )}
        </div>
      </CardContent>
    </Card>
  );
}
