import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import RatingStars from "@/components/RatingStars";
import SafeImage from "@/components/SafeImage";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ApiError, apiGet, apiGetOrNull } from "@/lib/apiClient";
import { formatCurrency } from "@/lib/format";
import { asArray, getNumber, getString, isRecord } from "@/lib/safe";
import { Link, useParams } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";

export default function ProductDetailPage() {
  const { productId, slug } = useParams();
  const productIdNumber = useMemo(() => Number(productId), [productId]);
  const slugValue = useMemo(() => (slug ?? "").trim(), [slug]);
  const isSlugRoute = useMemo(() => slugValue.length > 0, [slugValue]);

  const [product, setProduct] = useState<unknown>(null);
  const [images, setImages] = useState<unknown[]>([]);
  const [reviews, setReviews] = useState<unknown[]>([]);
  const [activeImageUrl, setActiveImageUrl] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      if (!isSlugRoute && (!Number.isFinite(productIdNumber) || productIdNumber <= 0)) {
        setError("Product not found");
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);

        const productEndpoint = isSlugRoute
          ? `/api/public/products/slug/${encodeURIComponent(slugValue)}`
          : `/api/public/products/${productIdNumber}`;

        let p = await apiGetOrNull<unknown>(productEndpoint);

        if (!p && isSlugRoute) {
          const resolved = await apiGetOrNull<{
            content?: unknown[];
          }>(
            `/api/public/products?slug=${encodeURIComponent(slugValue)}&page=0&size=1`
          );
          const first = resolved?.content?.[0];
          const resolvedId = getNumber(first, "id");
          if (resolvedId) {
            p = await apiGetOrNull<unknown>(`/api/public/products/${resolvedId}`);
          }
        }

        if (!isMounted) return;
        if (!p) {
          setError("Product not found");
          setIsLoading(false);
          return;
        }
        setProduct(p);

        const resolvedId = getNumber(p, "id") ?? productIdNumber;
        const [primaryImage, resolvedImages, resolvedReviews] = await Promise.all([
          apiGetOrNull<unknown>(`/api/public/products/${resolvedId}/images/primary`),
          apiGetOrNull<unknown[]>(`/api/public/products/${resolvedId}/images`),
          apiGetOrNull<unknown[]>(`/api/public/products/${resolvedId}/reviews`),
        ]);

        if (!isMounted) return;

        const imageList = resolvedImages ?? [];
        setImages(imageList);
        setReviews(resolvedReviews ?? []);

        const primary =
          primaryImage ??
          imageList.find((img) => isRecord(img) && img["isPrimary"] === true) ??
          imageList[0];
        const primaryUrl =
          getString(primary, "url", "imageUrl") ??
          getString(p, "primaryImageUrl", "imageUrl", "thumbnailUrl");
        setActiveImageUrl(primaryUrl ?? null);
      } catch (e) {
        if (!isMounted) return;
        if (e instanceof ApiError && e.status === 404) {
          setError("Product not found");
        } else {
          setError(e instanceof Error ? e.message : "Unknown error");
        }
      } finally {
        if (!isMounted) return;
        setIsLoading(false);
      }
    }

    void run();
    return () => {
      isMounted = false;
    };
  }, [isSlugRoute, productIdNumber, slugValue]);

  const productName = getString(product, "name", "title") ?? "Product";
  const price = getNumber(product, "salePrice", "price");
  const currency = getString(product, "currency");
  const rating = getNumber(product, "recentlyAverageRating", "rating");
  const status = getString(product, "status", "inStockStatus");
  const description = getString(product, "description", "shortDescription");
  const resolvedId = getNumber(product, "id") ?? productIdNumber;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Product</div>
          <h1 className="text-2xl font-bold tracking-tight">{productName}</h1>
          <div className="mt-2 flex flex-wrap items-center gap-2">
            <div className="text-xl font-bold text-primary">
              {formatCurrency(price, currency)}
            </div>
            {status ? (
              <Badge variant="secondary" className="bg-background/70 backdrop-blur">
                {status}
              </Badge>
            ) : null}
            <div className="flex items-center gap-2">
              <RatingStars rating={rating} />
              <span className="text-xs text-muted-foreground">#{resolvedId}</span>
            </div>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button asChild variant="secondary">
            <Link to="/products">
              <span className="inline-flex items-center gap-2">
                <svg
                  viewBox="0 0 24 24"
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="M15 18l-6-6 6-6" />
                </svg>
                Back
              </span>
            </Link>
          </Button>
          <Button asChild variant="outline">
            <Link to={`/products?categoryId=${getNumber(product, "categoryId") ?? ""}`}>
              <span className="inline-flex items-center gap-2">
                <svg
                  viewBox="0 0 24 24"
                  className="h-4 w-4"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                >
                  <path d="M4 6h7v7H4z" />
                  <path d="M13 6h7v7h-7z" />
                </svg>
                Category
              </span>
            </Link>
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="grid gap-4 lg:grid-cols-2">
          <LoadingCard />
          <LoadingCard />
        </div>
      ) : error ? (
        <EmptyState title="Failed to load product" description={error} />
      ) : (
        <div className="grid gap-6 lg:grid-cols-2">
          <div className="space-y-3">
            <div className="group overflow-hidden rounded-2xl border bg-gradient-to-br from-primary/10 via-background to-background shadow-sm transition hover:-translate-y-0.5 hover:shadow-lg">
              <div className="aspect-square">
                <SafeImage
                  src={activeImageUrl}
                  alt={productName}
                  fallbackKey={resolvedId}
                  className="h-full w-full object-cover transition duration-500 group-hover:scale-[1.02]"
                />
              </div>
            </div>

            {images.length ? (
              <div className="flex gap-2 overflow-auto pb-1">
                {images.map((img, idx) => {
                  const url = getString(img, "url", "imageUrl");
                  if (!url) return null;
                  const active = activeImageUrl === url;
                  return (
                    <button
                      key={String(idx)}
                      type="button"
                      onClick={() => setActiveImageUrl(url)}
                      className={`h-16 w-16 overflow-hidden rounded-lg border transition ${
                        active
                          ? "border-primary ring-2 ring-primary/30"
                          : "hover:border-foreground/20"
                      }`}
                      title={active ? "Selected" : "Select image"}
                    >
                      <SafeImage
                        src={url}
                        alt=""
                        fallbackKey={`${resolvedId}-${idx}`}
                        className="h-full w-full object-cover"
                      />
                    </button>
                  );
                })}
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">No images</div>
            )}
          </div>

          <div className="space-y-4">
            <Card className="shine">
              <CardHeader>
                <CardTitle className="text-base">Description</CardTitle>
              </CardHeader>
              <CardContent className="text-sm text-muted-foreground">
                {description ? (
                  <div className="whitespace-pre-line">{description}</div>
                ) : (
                  "No description"
                )}
              </CardContent>
            </Card>

            <Card className="shine">
              <CardHeader>
                <CardTitle className="text-base">Reviews</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {asArray(reviews).length === 0 ? (
                  <div className="text-sm text-muted-foreground">
                    Chưa có đánh giá nào. Hãy là người đầu tiên review sản phẩm này.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {asArray(reviews).slice(0, 8).map((r, idx) => {
                      const ratingValue = getNumber(r, "rating") ?? 0;
                      const comment = getString(r, "comment", "content") ?? "";
                      const username =
                        getString(r, "username", "userName", "author") ?? "User";
                      return (
                        <div
                          key={String(idx)}
                          className="shine rounded-xl border bg-card/80 p-3 backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md"
                        >
                          <div className="flex items-center justify-between gap-3">
                            <div className="text-sm font-medium">{username}</div>
                            <RatingStars rating={ratingValue} />
                          </div>
                          {comment ? (
                            <div className="mt-2 text-sm text-muted-foreground">{comment}</div>
                          ) : null}
                        </div>
                      );
                    })}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </div>
      )}
    </div>
  );
}
