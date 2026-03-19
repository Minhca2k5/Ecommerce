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
  const basePrice = getNumber(product, "price");
  const salePrice = getNumber(product, "salePrice");
  const price = salePrice ?? basePrice ?? 0;
  const currency = getString(product, "currency");
  const rating = getNumber(product, "recentlyAverageRating", "rating");
  const reviewCount = getNumber(product, "recentlyReviewCount", "reviewCount", "totalReviews");
  const soldCount = getNumber(product, "recentlyTotalSoldQuantity", "totalSold", "soldCount");

  const inStockText =
    getString(product, "inStockStatus") ??
    (getBoolean(product, "inStock") === false ? "Out of stock" : undefined);

  const hasDiscount =
    Number.isFinite(basePrice) &&
    Number.isFinite(salePrice) &&
    (salePrice ?? 0) > 0 &&
    (basePrice ?? 0) > (salePrice ?? 0);

  const discountPct =
    hasDiscount && basePrice
      ? Math.min(80, Math.max(5, Math.round(((basePrice - (salePrice ?? 0)) / basePrice) * 100)))
      : null;

  const isBestSeller = (soldCount ?? 0) >= 20;
  const isHot = (soldCount ?? 0) >= 30;
  const isTopRated = (rating ?? 0) >= 4.7 && (reviewCount ?? 0) >= 10;
  const freeShip = currency === "VND" ? price >= 300000 : price >= 20;
  const displayReviewCount = reviewCount ?? ((soldCount ?? 0) > 0 ? Math.max(12, Math.round((soldCount ?? 0) * 1.2)) : 0);
  const displayRating = rating ?? ((soldCount ?? 0) > 0 ? 4.8 : undefined);

  const imageUrl =
    getString(product, "primaryImageUrl", "imageUrl", "thumbnailUrl") ??
    getString(product, "url") ??
    (isRecord(product) ? (product["primaryImage"] as string | undefined) : undefined);
  const description = getString(product, "shortDescription", "description")?.trim();

  return (
    <Card className="pressable market-card group overflow-hidden border border-border bg-card transition duration-300 hover:-translate-y-0.5 hover:shadow-lg">
      <div className="relative aspect-[4/3] overflow-hidden bg-muted/40">
        <Link to={href} aria-label={`View ${name}`}>
          <SafeImage
            src={imageUrl}
            alt={name}
            fallbackKey={id || name}
            className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
          />
        </Link>

        <div className="absolute left-3 top-3 flex flex-wrap gap-2">
          {hasDiscount && discountPct ? (
            <Badge className="bg-primary text-primary-foreground">-{discountPct}%</Badge>
          ) : null}
          {isBestSeller ? (
            <Badge variant="secondary" className="bg-amber-100 text-amber-900">Best seller</Badge>
          ) : isTopRated ? (
            <Badge variant="secondary" className="bg-emerald-100 text-emerald-900">Top rated</Badge>
          ) : isHot ? (
            <Badge variant="secondary" className="bg-rose-100 text-rose-800">Hot</Badge>
          ) : null}
          {inStockText ? (
            <Badge variant="secondary" className="bg-background/95 text-foreground ring-1 ring-primary/15">
              {inStockText}
            </Badge>
          ) : null}
        </div>
      </div>

      <CardContent className="space-y-3 p-4">
        <div className="space-y-2">
          <Link to={href} className="line-clamp-2 text-[15px] font-medium leading-6 text-foreground/95 hover:text-primary">
            {name}
          </Link>
          <div className="flex flex-wrap items-end justify-between gap-3">
            <div className="flex items-center gap-2">
              <div className="text-lg font-bold text-primary">{formatCurrency(price, currency)}</div>
              {hasDiscount && basePrice ? (
                <div className="text-xs text-muted-foreground line-through">
                  {formatCurrency(basePrice, currency)}
                </div>
              ) : null}
            </div>
            {displayReviewCount > 0 ? (
              <div className="flex items-center gap-2">
                <RatingStars rating={displayRating} />
                <span className="text-[11px] text-muted-foreground">
                  {Number(displayRating ?? 0).toFixed(1)} ({formatCompactNumber(displayReviewCount)})
                </span>
              </div>
            ) : null}
          </div>
          <div className="flex min-h-4 flex-wrap items-center gap-2 text-[11px] text-muted-foreground">
            {soldCount ? <span className="font-medium text-muted-foreground/90">{formatCompactNumber(soldCount)} sold</span> : null}
            {description ? <span className="line-clamp-1 text-muted-foreground/85">{description}</span> : null}
            {freeShip ? <span className="rounded-full border border-emerald-200 bg-emerald-50 px-2 py-0.5 text-emerald-800">Free ship</span> : null}
          </div>
        </div>

        <div>
          <Button className="w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={() => addToCart(id, 1)} disabled={!id || isWorking}>
            Shop now
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
