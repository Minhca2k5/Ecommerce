import SafeImage from "@/components/SafeImage";
import RatingStars from "@/components/RatingStars";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { formatCurrency, formatCompactNumber } from "@/lib/format";
import { getBoolean, getNumber, getString, isRecord } from "@/lib/safe";
import { Link } from "react-router-dom";
import { useCartActions } from "@/lib/useCartActions";

export default function ProductCard({
  product,
  href,
}: {
  product: unknown;
  href: string;
}) {
  const { addToCart, isWorking } = useCartActions();
  const id = getNumber(product, "id") ?? 0;
  const name = getString(product, "name", "title") ?? "Product";
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
    <Card className="pressable group overflow-hidden border-primary/15 bg-card shadow-sm transition hover:shadow-md hover:shadow-primary/15">
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/40">
        <div className="absolute inset-x-0 top-0 z-10 h-1 bg-gradient-to-r from-primary/70 via-amber-400/70 to-cyan-500/70" />
        <SafeImage
          src={imageUrl}
          alt={name}
          fallbackKey={id || name}
          className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
        />

        <div className="absolute left-3 top-3 flex gap-2">
          {inStockText ? (
            <Badge variant="secondary" className="bg-background/95 text-foreground ring-1 ring-primary/15">
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
          <Button className="w-full rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => addToCart(id, 1)} disabled={!id || isWorking}>
            Add
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
