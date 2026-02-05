import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import RatingStars from "@/components/RatingStars";
import SafeImage from "@/components/SafeImage";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGetOrNull } from "@/lib/apiClient";
import { ApiError } from "@/lib/apiError";
import { formatCurrency } from "@/lib/format";
import { asArray, getBoolean, getNumber, getString, isRecord } from "@/lib/safe";
import { Link, useParams } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/app/AuthProvider";
import { useCartActions } from "@/lib/useCartActions";
import { useToast } from "@/app/ToastProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { addRecentView } from "@/lib/recentViewApi";
import { addWishlist } from "@/lib/wishlistApi";
import { createMyReview, deleteMyReview, listMyReviews, updateMyReviewComment, updateMyReviewRating, type ReviewResponse } from "@/lib/reviewApi";
import { getErrorMessage } from "@/lib/errors";
import ConfirmDialog from "@/components/ConfirmDialog";
import Modal from "@/components/Modal";

export default function ProductDetailPage() {
  const auth = useAuth();
  const { addToCart, isWorking } = useCartActions();
  const toast = useToast();
  const notifications = useNotifications();
  const { productId, slug } = useParams();
  const productIdNumber = useMemo(() => Number(productId), [productId]);
  const slugValue = useMemo(() => (slug ?? "").trim(), [slug]);
  const isSlugRoute = useMemo(() => slugValue.length > 0, [slugValue]);

  const [product, setProduct] = useState<unknown>(null);
  const [images, setImages] = useState<unknown[]>([]);
  const [reviews, setReviews] = useState<unknown[]>([]);
  const [myReviews, setMyReviews] = useState<ReviewResponse[]>([]);
  const [myRating, setMyRating] = useState<number>(5);
  const [myComment, setMyComment] = useState<string>("");
  const [editingReviewId, setEditingReviewId] = useState<number | null>(null);
  const [deleteReviewId, setDeleteReviewId] = useState<number | null>(null);
  const [editDraftRating, setEditDraftRating] = useState<number>(5);
  const [editDraftComment, setEditDraftComment] = useState<string>("");
  const [isSavingReview, setIsSavingReview] = useState(false);
  const [isWishlistWorking, setIsWishlistWorking] = useState(false);
  const [activeImageUrl, setActiveImageUrl] = useState<string | null>(null);
  const [activeImageId, setActiveImageId] = useState<number | null>(null);
  const [isImageDetailOpen, setIsImageDetailOpen] = useState(false);
  const [imageDetail, setImageDetail] = useState<unknown>(null);
  const [imageDetailError, setImageDetailError] = useState<string | null>(null);
  const [isImageDetailLoading, setIsImageDetailLoading] = useState(false);
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
        const reviewList = resolvedReviews ?? [];
        setReviews(reviewList);
        syncProductRatingFromReviews(reviewList);

        const primary =
          primaryImage ??
          imageList.find((img) => isRecord(img) && img["isPrimary"] === true) ??
          imageList[0];
        const primaryUrl =
          getString(primary, "url", "imageUrl") ??
          getString(p, "primaryImageUrl", "imageUrl", "thumbnailUrl");
        const primaryId = getNumber(primary, "id");
        setActiveImageUrl(primaryUrl ?? null);
        setActiveImageId(primaryId ?? null);

        if (auth.isAuthenticated && resolvedId) {
          addRecentView({ productId: Number(resolvedId) }).catch(() => {
            // ignore
          });
          listMyReviews()
            .then((list) => {
              const mine = (list ?? []).filter((r) => Number(r.productId) === Number(resolvedId));
              setMyReviews(mine);
              setEditingReviewId(null);
              setMyRating(5);
              setMyComment("");
            })
            .catch(() => {
              // ignore
            });
        }
      } catch (e) {
        if (!isMounted) return;
        if (e instanceof ApiError && e.status === 404) {
          setError("Product not found");
        } else {
          setError(getErrorMessage(e, "Failed to load product."));
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
  }, [auth.isAuthenticated, isSlugRoute, productIdNumber, slugValue]);

  const productName = getString(product, "name", "title") ?? "Product";
  const price = getNumber(product, "salePrice", "price");
  const currency = getString(product, "currency");
  const rating = getNumber(product, "recentlyAverageRating", "rating");
  const status = getString(product, "status", "inStockStatus");
  const description = getString(product, "description", "shortDescription");
  const resolvedId = getNumber(product, "id") ?? productIdNumber;

  function syncProductRatingFromReviews(nextReviews: unknown[]) {
    const list = asArray(nextReviews);
    const values = list
      .map((r) => Number(getNumber(r, "rating") ?? 0))
      .filter((n) => Number.isFinite(n) && n > 0);
    const average = values.length ? values.reduce((a, b) => a + b, 0) / values.length : 0;
    const rounded = Math.round(average * 10) / 10;
    setProduct((prev: unknown) => (isRecord(prev) ? ({ ...(prev as any), recentlyAverageRating: rounded } as unknown) : prev));
  }

  async function refreshPublicReviews() {
    if (!resolvedId) return;
    const next = await apiGetOrNull<unknown[]>(`/api/public/products/${resolvedId}/reviews`);
    const list = next ?? [];
    setReviews(list);
    syncProductRatingFromReviews(list);
  }

  async function onAddWishlist() {
    if (!resolvedId) return;
    setIsWishlistWorking(true);
    try {
      const w = await addWishlist({ productId: Number(resolvedId) });
      toast.push({ variant: "success", title: "Wishlisted", message: "Saved to your wishlist." });
      notifications.push({
        type: "PRODUCT",
        title: "Saved to wishlist",
        message: `${w.productName || productName} saved to wishlist.`,
        referenceId: Number(resolvedId),
        referenceType: "PRODUCT",
      });
    } catch (e) {
      toast.push({ variant: "error", title: "Wishlist failed", message: getErrorMessage(e, "This product is already in your wishlist.") });
    } finally {
      setIsWishlistWorking(false);
    }
  }

  async function onSaveReview() {
    if (!resolvedId) return;
    setIsSavingReview(true);
    try {
      const editId = editingReviewId ? Number(editingReviewId) : 0;
      if (!editId) {
        const created = await createMyReview({ productId: Number(resolvedId), rating: myRating, comment: myComment.trim() || undefined });
        setMyReviews((prev) => [created, ...prev]);
        setReviews((prev) => {
          const next = [created as unknown, ...(prev ?? [])];
          syncProductRatingFromReviews(next);
          return next;
        });
        toast.push({ variant: "success", title: "Review posted", message: "Thanks for your feedback!" });
        notifications.push({
          type: "REVIEW",
          title: "Review submitted",
          message: `You rated ${productName} ${myRating}/5.`,
          referenceId: Number(resolvedId),
          referenceType: "PRODUCT",
        });
      } else {
        const current = myReviews.find((r) => Number(r.id) === editId);
        if (current && Number(current.rating) !== Number(myRating)) await updateMyReviewRating(editId, myRating);
        if (current && String(current.comment || "") !== String(myComment || "")) await updateMyReviewComment(editId, myComment);
        setMyReviews((prev) => prev.map((r) => (Number(r.id) === editId ? { ...r, rating: myRating, comment: myComment } : r)));
        setReviews((prev) => {
          const next = (prev ?? []).map((r) => {
            const id = getNumber(r, "id");
            if (id && Number(id) === editId) return { ...(r as any), rating: myRating, comment: myComment } as unknown;
            return r;
          });
          syncProductRatingFromReviews(next);
          return next;
        });
        toast.push({ variant: "success", title: "Review updated", message: "Your review has been updated." });
        notifications.push({
          type: "REVIEW",
          title: "Review updated",
          message: `Updated your review for ${productName}.`,
          referenceId: Number(resolvedId),
          referenceType: "PRODUCT",
        });
      }

      const refreshed = await listMyReviews().catch(() => [] as ReviewResponse[]);
      setMyReviews((refreshed ?? []).filter((r) => Number(r.productId) === Number(resolvedId)));
      setEditingReviewId(null);
      setMyRating(5);
      setMyComment("");
      await refreshPublicReviews();
    } catch (e) {
      toast.push({ variant: "error", title: "Review failed", message: getErrorMessage(e, "Failed to save review.") });
    } finally {
      setIsSavingReview(false);
    }
  }

  async function onDeleteReview(reviewId?: number) {
    const id = Number(reviewId ?? editingReviewId ?? 0);
    if (!id) return;
    setIsSavingReview(true);
    try {
      await deleteMyReview(id);
      toast.push({ variant: "success", title: "Review deleted", message: "Your review has been removed." });
      notifications.push({
        type: "REVIEW",
        title: "Review deleted",
        message: `Removed your review for ${productName}.`,
        referenceId: Number(resolvedId),
        referenceType: "PRODUCT",
      });
      setMyReviews((prev) => prev.filter((r) => Number(r.id) !== id));
      setReviews((prev) => {
        const next = (prev ?? []).filter((r) => Number(getNumber(r, "id") ?? 0) !== id);
        syncProductRatingFromReviews(next);
        return next;
      });
      setEditingReviewId(null);
      setMyRating(5);
      setMyComment("");
      await refreshPublicReviews();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete review.") });
    } finally {
      setIsSavingReview(false);
    }
  }

  async function openImageDetails() {
    if (!resolvedId || !activeImageId) return;
    setIsImageDetailOpen(true);
    setImageDetail(null);
    setImageDetailError(null);
    setIsImageDetailLoading(true);
    try {
      const d = await apiGetOrNull<unknown>(`/api/public/products/${resolvedId}/images/${activeImageId}`);
      setImageDetail(d);
    } catch (e) {
      setImageDetailError(getErrorMessage(e, "Failed to load image details."));
    } finally {
      setIsImageDetailLoading(false);
    }
  }

  return (
    <div className="space-y-8">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">Product</div>
            <h1 className="text-2xl font-bold tracking-tight sm:text-3xl">{productName}</h1>
            <div className="mt-2 flex flex-wrap items-center gap-2">
              <div className="text-xl font-bold text-primary">{formatCurrency(price, currency)}</div>
              {status ? (
                <Badge variant="secondary" className="bg-background/70 backdrop-blur">
                  {status}
                </Badge>
              ) : null}
              <div className="flex items-center gap-2">
                <RatingStars rating={rating} />
                <span className="text-xs text-muted-foreground">Verified</span>
              </div>
            </div>
          </div>

          <div className="flex flex-wrap items-center gap-2">
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
          <Button
            className="h-10 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
            onClick={() => addToCart(resolvedId, 1)}
            disabled={!resolvedId || isWorking || Boolean(error) || isLoading}
          >
            Add to cart
          </Button>
          {auth.isAuthenticated ? (
            <Button
              variant="outline"
              className="h-10 rounded-xl bg-background/70 backdrop-blur"
              onClick={onAddWishlist}
              disabled={!resolvedId || isWishlistWorking || Boolean(error) || isLoading}
            >
              Wishlist
            </Button>
          ) : null}
          <Button asChild variant="outline" className="h-10 rounded-xl bg-background/70 backdrop-blur">
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
      </section>

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
            <div className="group overflow-hidden rounded-3xl border bg-background/70 shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:shadow-lg">
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
              <div className="space-y-2">
                <div className="flex items-center justify-between">
                  <div className="text-xs text-muted-foreground">Gallery</div>
                  <Button
                    type="button"
                    variant="outline"
                    className="h-9 rounded-xl bg-background/70 backdrop-blur"
                    onClick={() => void openImageDetails()}
                    disabled={!activeImageId}
                  >
                    Image details
                  </Button>
                </div>
                <div className="flex gap-2 overflow-auto pb-2 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden snap-x snap-mandatory">
                  {images.map((img, idx) => {
                    const url = getString(img, "url", "imageUrl");
                    if (!url) return null;
                    const imgId = getNumber(img, "id");
                    const active = activeImageUrl === url;
                    return (
                      <button
                        key={String(idx)}
                        type="button"
                        onClick={() => {
                          setActiveImageUrl(url);
                          setActiveImageId(imgId ?? null);
                        }}
                        className={`h-16 w-16 shrink-0 snap-start overflow-hidden rounded-xl border bg-background/70 transition ${
                          active ? "border-primary ring-2 ring-primary/30" : "hover:border-foreground/20"
                        }`}
                        title={active ? "Selected" : "Select image"}
                      >
                        <SafeImage src={url} alt="" fallbackKey={`${resolvedId}-${idx}`} className="h-full w-full object-cover" />
                      </button>
                    );
                  })}
                </div>
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">No images</div>
            )}
          </div>

          <div className="space-y-4">
            <Card className="shine bg-background/70 backdrop-blur">
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

            <Card className="shine bg-background/70 backdrop-blur">
              <CardHeader>
                <CardTitle className="text-base">Reviews</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                {auth.isAuthenticated ? (
                  <div className="rounded-2xl border bg-background/60 p-3 backdrop-blur">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <div className="text-sm font-medium">Write a review</div>
                        <div className="mt-1 text-xs text-muted-foreground">Your comment appears instantly after posting.</div>
                      </div>
                      <div className="flex items-center gap-1">
                        {Array.from({ length: 5 }).map((_, i) => {
                          const v = i + 1;
                          const active = v <= myRating;
                          return (
                            <button
                              key={v}
                              type="button"
                              className={active ? "text-amber-500" : "text-muted-foreground"}
                              onClick={() => setMyRating(v)}
                              aria-label={`Rate ${v}`}
                            >
                              ★
                            </button>
                          );
                        })}
                      </div>
                    </div>
                    <div className="mt-3 space-y-2">
                      <textarea
                        value={myComment}
                        onChange={(e) => setMyComment(e.target.value)}
                        className="min-h-20 w-full rounded-xl border bg-background px-3 py-2 text-sm"
                        placeholder="Write a short comment..."
                      />
                      <Button
                        disabled={isSavingReview}
                        onClick={onSaveReview}
                        className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
                      >
                        {isSavingReview ? "Posting..." : "Post"}
                      </Button>
                    </div>

                    {null}
                  </div>
                ) : null}
                {asArray(reviews).length === 0 ? (
                  <div className="text-sm text-muted-foreground">
                    Chưa có đánh giá nào. Hãy là người đầu tiên review sản phẩm này.
                  </div>
                ) : (
                  <div className="space-y-3">
                    {asArray(reviews).slice(0, 8).map((r, idx) => {
                      const ratingValue = getNumber(r, "rating") ?? 0;
                      const comment = getString(r, "comment", "content") ?? "";
                      const username = getString(r, "username", "userName", "author") ?? "User";
                      const reviewId = getNumber(r, "id") ?? 0;
                      const isMine =
                        auth.isAuthenticated &&
                        ((getNumber(r, "userId") !== undefined && getNumber(r, "userId") === auth.user?.id) ||
                          username === auth.user?.username);
                      const displayName = isMine ? "You" : username;
                      const isEditing = Boolean(reviewId) && editingReviewId === Number(reviewId);
                      return (
                        <div
                          key={String(idx)}
                          className="group shine rounded-xl border bg-card/80 p-3 backdrop-blur transition hover:-translate-y-0.5 hover:shadow-md"
                        >
                          <div className="flex items-start justify-between gap-3">
                            <div className="text-sm font-medium">{displayName}</div>
                            <div className="flex items-center gap-2">
                              <RatingStars rating={isEditing ? editDraftRating : ratingValue} />
                              {isMine && reviewId ? (
                                <div className="flex items-center gap-1 opacity-0 transition-opacity group-hover:opacity-100">
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 rounded-lg px-2 text-xs"
                                    onClick={() => {
                                      setEditingReviewId(Number(reviewId));
                                      setEditDraftRating(ratingValue || 5);
                                      setEditDraftComment(comment);
                                    }}
                                  >
                                    Edit
                                  </Button>
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="sm"
                                    className="h-8 rounded-lg px-2 text-xs text-rose-600 hover:bg-rose-500/10 hover:text-rose-700"
                                    onClick={() => setDeleteReviewId(Number(reviewId))}
                                  >
                                    Delete
                                  </Button>
                                </div>
                              ) : null}
                            </div>
                          </div>
                          {isEditing ? (
                            <div className="mt-3 space-y-2">
                              <div className="flex items-center gap-1">
                                {Array.from({ length: 5 }).map((_, i) => {
                                  const v = i + 1;
                                  const active = v <= editDraftRating;
                                  return (
                                    <button
                                      key={v}
                                      type="button"
                                      className={active ? "text-amber-500" : "text-muted-foreground"}
                                      onClick={() => setEditDraftRating(v)}
                                      aria-label={`Rate ${v}`}
                                    >
                                      ★
                                    </button>
                                  );
                                })}
                              </div>
                              <textarea
                                value={editDraftComment}
                                onChange={(e) => setEditDraftComment(e.target.value)}
                                className="min-h-16 w-full rounded-xl border bg-background px-3 py-2 text-sm"
                              />
                              <div className="flex gap-2">
                                <Button
                                  disabled={isSavingReview}
                                  className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
                                  onClick={async () => {
                                    if (!reviewId) return;
                                    setIsSavingReview(true);
                                    try {
                                      if (Number(ratingValue) !== Number(editDraftRating)) await updateMyReviewRating(Number(reviewId), editDraftRating);
                                      if (String(comment || "") !== String(editDraftComment || "")) await updateMyReviewComment(Number(reviewId), editDraftComment);
                                      setReviews((prev) => {
                                        const next = (prev ?? []).map((x) => {
                                          const rid = getNumber(x, "id");
                                          if (rid && Number(rid) === Number(reviewId)) return { ...(x as any), rating: editDraftRating, comment: editDraftComment } as unknown;
                                          return x;
                                        });
                                        syncProductRatingFromReviews(next);
                                        return next;
                                      });
                                      toast.push({ variant: "success", title: "Updated", message: "Review updated." });
                                      setEditingReviewId(null);
                                      await refreshPublicReviews();
                                    } catch (e) {
                                      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update review.") });
                                    } finally {
                                      setIsSavingReview(false);
                                    }
                                  }}
                                >
                                  Save
                                </Button>
                                <Button type="button" variant="outline" className="rounded-xl" onClick={() => setEditingReviewId(null)}>
                                  Cancel
                                </Button>
                              </div>
                            </div>
                          ) : comment ? (
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

      <ConfirmDialog
        isOpen={Boolean(deleteReviewId)}
        title="Delete this review?"
        description="This action cannot be undone."
        confirmText="Delete"
        variant="danger"
        isLoading={isSavingReview}
        onClose={() => setDeleteReviewId(null)}
        onConfirm={() => {
          const id = Number(deleteReviewId ?? 0);
          if (!id) return;
          void onDeleteReview(id).finally(() => setDeleteReviewId(null));
        }}
      />

      <Modal isOpen={isImageDetailOpen} onClose={() => setIsImageDetailOpen(false)} title="Image details">
        <div className="space-y-3">
          {isImageDetailLoading ? <div className="text-sm text-muted-foreground">Loading...</div> : null}
          {imageDetailError ? (
            <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{imageDetailError}</div>
          ) : null}
          {imageDetail ? (
            <div className="rounded-2xl border bg-background/70 p-3 text-sm backdrop-blur">
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Image</span>
                <span className="font-medium">Details</span>
              </div>
              <div className="mt-2 flex items-center justify-between">
                <span className="text-muted-foreground">Primary</span>
                <span className="font-medium">{String(getBoolean(imageDetail, "isPrimary") ?? false)}</span>
              </div>
              <div className="mt-2 break-all text-xs text-muted-foreground">{getString(imageDetail, "url", "imageUrl") ?? "-"}</div>
            </div>
          ) : null}
          <Button variant="outline" className="h-10 w-full rounded-xl bg-background/70 backdrop-blur" onClick={() => setIsImageDetailOpen(false)}>
            Close
          </Button>
        </div>
      </Modal>
    </div>
  );
}
