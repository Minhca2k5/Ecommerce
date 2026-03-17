import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import ProductCard from "@/components/ProductCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { apiGet, buildQuery } from "@/lib/apiClient";
import type { SpringPage } from "@/lib/pagination";
import { getNumber, getString } from "@/lib/safe";
import { useSearchParams } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { addSearchLog } from "@/lib/searchLogApi";
import { getErrorMessage } from "@/lib/errors";
import { createAuthedEventSource } from "@/lib/sse";

type ProductSummary = unknown;
type CategorySummary = unknown;
type FlashSaleSummary = {
  status?: string;
  headline?: string;
  description?: string;
  endAt?: string;
  soldToday?: number;
  soldTarget?: number;
  leftCount?: number;
  soldPercent?: number;
};

function formatCountdown(ms: number) {
  const safe = Math.max(0, Math.floor(ms / 1000));
  const h = Math.floor(safe / 3600);
  const m = Math.floor((safe % 3600) / 60);
  const s = safe % 60;
  return `${String(h).padStart(2, "0")}:${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function soldBarWidthClass(percent: number) {
  if (percent >= 100) return "w-full";
  if (percent >= 90) return "w-[90%]";
  if (percent >= 80) return "w-[80%]";
  if (percent >= 70) return "w-[70%]";
  if (percent >= 60) return "w-[60%]";
  if (percent >= 50) return "w-1/2";
  if (percent >= 40) return "w-[40%]";
  if (percent >= 30) return "w-[30%]";
  if (percent >= 20) return "w-[20%]";
  if (percent >= 10) return "w-[10%]";
  return "w-0";
}

export default function ProductsPage() {
  const auth = useAuth();
  const toast = useToast();
  const [searchParams, setSearchParams] = useSearchParams();

  const page = Number(searchParams.get("page") ?? "0");
  const size = Number(searchParams.get("size") ?? "12");
  const name = searchParams.get("name") ?? "";
  const categoryId = searchParams.get("categoryId") ?? "";

  const [data, setData] = useState<SpringPage<ProductSummary> | null>(null);
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasNewProducts, setHasNewProducts] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [sortKey, setSortKey] = useState("best-match");
  const [rankedProducts, setRankedProducts] = useState<ProductSummary[]>([]);
  const [isRankLoading, setIsRankLoading] = useState(false);
  const [flashSale, setFlashSale] = useState<FlashSaleSummary | null>(null);
  const [dealFilters, setDealFilters] = useState({
    freeShip: false,
    topRated: false,
    bestSeller: false,
  });
  const [nowTs, setNowTs] = useState(() => Date.now());

  const queryString = useMemo(
    () =>
      buildQuery({
        page,
        size,
        name: name.trim() ? name.trim() : undefined,
        categoryId: categoryId.trim() ? categoryId.trim() : undefined,
      }),
    [page, size, name, categoryId]
  );

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setIsLoading(true);
        setError(null);
        const [result, categoryPage, flashSaleData] = await Promise.all([
          apiGet<SpringPage<ProductSummary>>(`/api/public/products${queryString}`),
          apiGet<SpringPage<CategorySummary>>(
            `/api/public/categories${buildQuery({ page: 0, size: 50 })}`
          ),
          apiGet<FlashSaleSummary>(`/api/public/products/flash-sale`),
        ]);
        if (!isMounted) return;
        setData(result);
        setCategories(categoryPage?.content ?? []);
        setFlashSale(flashSaleData ?? null);
      } catch (e) {
        if (!isMounted) return;
        setError(getErrorMessage(e, "Failed to load products."));
      } finally {
        if (!isMounted) return;
        setIsLoading(false);
      }
    }

    void run();
    return () => {
      isMounted = false;
    };
  }, [queryString, refreshKey]);

  useEffect(() => {
    const id = window.setInterval(() => setNowTs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, []);

  useEffect(() => {
    const es = createAuthedEventSource("/api/public/realtime/new-products");
    const announce = () => {
      setHasNewProducts((prev) => {
        if (!prev) {
          toast.push({ variant: "default", title: "New products", message: "Fresh items are available." });
        }
        return true;
      });
    };
    es.addEventListener("product-created", announce);
    es.addEventListener("new-arrival", announce);
    es.addEventListener("product-updated", announce);
    es.onerror = () => {
      // ignore; browser auto-reconnects
    };
    return () => {
      es.close();
    };
  }, [toast]);

  useEffect(() => {
    if (!auth.isAuthenticated) return;
    const keyword = name.trim();
    if (!keyword) return;
    const handle = window.setTimeout(() => {
      addSearchLog({ keyword }).catch(() => {
        // ignore
      });
    }, 700);
    return () => window.clearTimeout(handle);
  }, [auth.isAuthenticated, name]);

  useEffect(() => {
    const rankedMode = sortKey === "rating" || sortKey === "sold";
    if (!rankedMode) {
      setRankedProducts([]);
      return;
    }

    let alive = true;
    setIsRankLoading(true);
    const endpoint = sortKey === "rating" ? "/api/public/products/top-rating" : "/api/public/products/best-selling";

    apiGet<ProductSummary[]>(endpoint)
      .then((rows) => {
        if (!alive) return;
        setRankedProducts(Array.isArray(rows) ? rows : []);
      })
      .catch(() => {
        if (!alive) return;
        setRankedProducts([]);
      })
      .finally(() => {
        if (!alive) return;
        setIsRankLoading(false);
      });

    return () => {
      alive = false;
    };
  }, [sortKey]);

  useEffect(() => {
    const next = new URLSearchParams(searchParams);
    if (next.get("page") !== "0") {
      next.set("page", "0");
      setSearchParams(next, { replace: true });
    }
  }, [sortKey, searchParams, setSearchParams]);

  const products = data?.content ?? [];
  const isRankedMode = sortKey === "rating" || sortKey === "sold";
  const sourceProducts = isRankedMode ? rankedProducts : products;

  const normalizedName = name.trim().toLowerCase();
  const selectedCategoryId = categoryId.trim();

  const queryFilteredProducts = useMemo(() => {
    if (!isRankedMode) return sourceProducts;
    return sourceProducts.filter((p) => {
      if (normalizedName) {
        const productName = (getString(p, "name", "title") ?? "").toLowerCase();
        if (!productName.includes(normalizedName)) return false;
      }
      if (selectedCategoryId) {
        const catId =
          getNumber(p, "categoryId") ??
          (typeof p === "object" && p !== null ? getNumber((p as any)["category"], "id") : undefined) ??
          undefined;
        if (String(catId ?? "") !== selectedCategoryId) return false;
      }
      return true;
    });
  }, [isRankedMode, normalizedName, selectedCategoryId, sourceProducts]);

  const filteredProducts = useMemo(() => {
    if (!dealFilters.freeShip && !dealFilters.topRated && !dealFilters.bestSeller) return queryFilteredProducts;
    return queryFilteredProducts.filter((p) => {
      const price = getNumber(p, "salePrice", "price") ?? 0;
      const currency = getString(p, "currency") ?? "VND";
      const rating = getNumber(p, "recentlyAverageRating", "rating") ?? 0;
      const reviewCount = getNumber(p, "recentlyReviewCount", "reviewCount", "totalReviews") ?? 0;
      const sold = getNumber(p, "recentlyTotalSoldQuantity", "totalSold", "soldCount") ?? 0;
      const freeShip = currency === "VND" ? price >= 300000 : price >= 20;
      if (dealFilters.freeShip && !freeShip) return false;
      if (dealFilters.topRated && !(rating >= 4.5 && reviewCount >= 5)) return false;
      if (dealFilters.bestSeller && !(sold >= 30)) return false;
      return true;
    });
  }, [dealFilters, queryFilteredProducts]);

  const sortedProducts = useMemo(() => {
    const list = [...filteredProducts];
    switch (sortKey) {
      case "price-low":
        return list.sort((a, b) => (getNumber(a, "salePrice", "price") ?? 0) - (getNumber(b, "salePrice", "price") ?? 0));
      case "price-high":
        return list.sort((a, b) => (getNumber(b, "salePrice", "price") ?? 0) - (getNumber(a, "salePrice", "price") ?? 0));
      case "rating":
        return list;
      case "sold":
        return list;
      default:
        return list;
    }
  }, [filteredProducts, sortKey]);

  const totalPagesForRanked = Math.max(1, Math.ceil(sortedProducts.length / Math.max(1, size)));
  const pagedProducts = useMemo(() => {
    if (!isRankedMode) return sortedProducts;
    const start = page * size;
    return sortedProducts.slice(start, start + size);
  }, [isRankedMode, page, size, sortedProducts]);

  const isBusy = isLoading || isRankLoading;
  const isNextDisabled = isBusy || (isRankedMode ? page + 1 >= totalPagesForRanked : Boolean(data?.last));

  const soldToday = useMemo(
    () =>
      sortedProducts.reduce<number>(
        (sum, p) => sum + (getNumber(p, "recentlyTotalSoldQuantity", "totalSold", "soldCount") ?? 0),
        0
      ),
    [sortedProducts]
  );
  const fallbackSoldTarget = Math.max(20, Math.ceil((soldToday + 5) / 10) * 10);
  const fallbackSoldPct = Math.min(100, Math.round((soldToday / fallbackSoldTarget) * 100));
  const fallbackLeftCount = Math.max(0, fallbackSoldTarget - soldToday);

  const soldPct = Math.max(0, Math.min(100, Number(flashSale?.soldPercent ?? fallbackSoldPct)));
  const leftCount = Math.max(0, Number(flashSale?.leftCount ?? fallbackLeftCount));
  const soldTodayDisplay = Math.max(0, Number(flashSale?.soldToday ?? soldToday));
  const endAtTs = flashSale?.endAt ? Date.parse(flashSale.endAt) : NaN;
  const remainingMs = Number.isFinite(endAtTs) ? endAtTs - nowTs : 0;
  const isFlashSaleEnded = String(flashSale?.status ?? "").toUpperCase() === "ENDED" || remainingMs <= 0;
  const flashSaleHeadline = flashSale?.headline?.trim() || (isFlashSaleEnded ? "Flash sale has ended" : "Grab deals before they are gone");
  const flashSaleDescription = flashSale?.description?.trim() || (isFlashSaleEnded ? "The next promotion will be available soon." : "Free shipping on select items today.");
  const countdown = formatCountdown(remainingMs);

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Shop products</h1>
            {name.trim() ? (
              <div className="mt-1 text-sm text-muted-foreground">Results for "{name.trim()}"</div>
            ) : null}
          </div>

          <div className="flex flex-wrap items-center gap-2">
            <Button
              type="button"
              variant="outline"
              className="h-9 rounded-md bg-background"
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.delete("name");
                next.delete("categoryId");
                next.set("page", "0");
                setSearchParams(next, { replace: true });
              }}
              disabled={isLoading || (!name.trim() && !categoryId.trim())}
            >
              Reset filters
            </Button>
            <Button
              type="button"
              variant="outline"
              className="h-9 rounded-md bg-background"
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.set("page", String(Math.max(0, page - 1)));
                setSearchParams(next, { replace: true });
              }}
              disabled={isBusy || page <= 0}
            >
              Prev
            </Button>
            <Button
              type="button"
              variant="outline"
              className="h-9 rounded-md bg-background"
              onClick={() => {
                const next = new URLSearchParams(searchParams);
                next.set("page", String(page + 1));
                setSearchParams(next, { replace: true });
              }}
              disabled={isNextDisabled}
            >
              Next
            </Button>
          </div>
        </div>
      </section>

      {hasNewProducts ? (
        <div className="rounded-md border bg-background p-4 text-sm shadow-sm">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="text-sm font-medium">New arrivals are live.</div>
            <Button
              type="button"
              className="rounded-md bg-primary text-primary-foreground"
              onClick={() => {
                setHasNewProducts(false);
                setRefreshKey((prev) => prev + 1);
              }}
            >
              Refresh
            </Button>
          </div>
        </div>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
        <Card className="h-fit bg-background lg:sticky lg:top-24">
          <CardHeader>
            <CardTitle className="text-base">Filters</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Input
                placeholder="Search products"
                value={name}
                onChange={(e) => {
                  const next = new URLSearchParams(searchParams);
                  next.set("name", e.target.value);
                  next.set("page", "0");
                  setSearchParams(next, { replace: true });
                }}
              />
            </div>

            <div className="space-y-2">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Categories</div>
              <ul className="space-y-2 text-sm">
                <li>
                  <label
                    className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 transition ${
                      !categoryId ? "bg-orange-50 text-orange-700" : "hover:bg-muted"
                    }`}
                  >
                    <input
                      type="radio"
                      className="accent-orange-500"
                      checked={!categoryId}
                      onChange={() => {
                        const next = new URLSearchParams(searchParams);
                        next.delete("categoryId");
                        next.set("page", "0");
                        setSearchParams(next, { replace: true });
                      }}
                    />
                    <span>All categories</span>
                  </label>
                </li>
                {categories.map((c, index) => {
                  const id = String(getNumber(c, "id") ?? index + 1);
                  const label = getString(c, "name", "title") ?? `Category ${id}`;
                  return (
                    <li key={id}>
                      <label
                        className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 transition ${
                          categoryId === id ? "bg-orange-50 text-orange-700" : "hover:bg-muted"
                        }`}
                      >
                        <input
                          type="radio"
                          className="accent-orange-500"
                          checked={categoryId === id}
                          onChange={() => {
                            const next = new URLSearchParams(searchParams);
                            next.set("categoryId", id);
                            next.set("page", "0");
                            setSearchParams(next, { replace: true });
                          }}
                        />
                        <span>{label}</span>
                      </label>
                    </li>
                  );
                })}
              </ul>
            </div>

            <div className="space-y-2">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Deals</div>
              <ul className="space-y-2 text-sm">
                <li>
                  <label className="flex cursor-pointer items-center gap-2">
                    <input
                      type="checkbox"
                      checked={dealFilters.freeShip}
                      onChange={(e) => setDealFilters((prev) => ({ ...prev, freeShip: e.target.checked }))}
                    />
                    <span>Free shipping</span>
                  </label>
                </li>
                <li>
                  <label className="flex cursor-pointer items-center gap-2">
                    <input
                      type="checkbox"
                      checked={dealFilters.topRated}
                      onChange={(e) => setDealFilters((prev) => ({ ...prev, topRated: e.target.checked }))}
                    />
                    <span>Top rated 4.5+</span>
                  </label>
                </li>
                <li>
                  <label className="flex cursor-pointer items-center gap-2">
                    <input
                      type="checkbox"
                      checked={dealFilters.bestSeller}
                      onChange={(e) => setDealFilters((prev) => ({ ...prev, bestSeller: e.target.checked }))}
                    />
                    <span>Best sellers</span>
                  </label>
                </li>
              </ul>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-4">
          {isBusy ? (
            <div className="rounded-md border bg-gradient-to-r from-amber-50 via-orange-50 to-white p-4 shadow-sm">
              <div className="space-y-3 animate-pulse">
                <div className="h-3 w-24 rounded bg-amber-200/80" />
                <div className="h-6 w-72 rounded bg-amber-200/70" />
                <div className="h-3 w-56 rounded bg-amber-100/90" />
                <div className="h-2 w-full max-w-md rounded bg-amber-100" />
              </div>
            </div>
          ) : (
            <div className="rounded-md border bg-gradient-to-r from-amber-50 via-orange-50 to-white p-4 shadow-sm">
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <div className="text-xs font-semibold uppercase tracking-[0.2em] text-amber-700">Flash sale</div>
                  <div className="text-lg font-semibold">{flashSaleHeadline}</div>
                  <div className="text-xs text-muted-foreground">{flashSaleDescription}</div>
                  {!isFlashSaleEnded ? (
                    <>
                      <div className="mt-2 flex flex-wrap items-center gap-3 text-xs">
                        <span className="rounded-full bg-rose-100 px-2 py-1 font-semibold text-rose-700">Only {leftCount} left</span>
                        <span className="rounded-full bg-emerald-100 px-2 py-1 font-semibold text-emerald-700">{soldTodayDisplay} sold today</span>
                      </div>
                      <div className="mt-2 h-2 w-full max-w-md overflow-hidden rounded-full bg-amber-100">
                        <div className={`h-full rounded-full bg-amber-500 transition-all ${soldBarWidthClass(soldPct)}`} />
                      </div>
                      <div className="mt-1 text-xs font-medium text-amber-700">{soldPct}% sold</div>
                    </>
                  ) : null}
                </div>
                {isFlashSaleEnded ? (
                  <div className="inline-flex items-center gap-2 rounded-md bg-slate-500 px-3 py-2 text-xs font-semibold text-white">
                    Ended
                  </div>
                ) : (
                  <div className="inline-flex items-center gap-2 rounded-md bg-amber-500 px-3 py-2 text-xs font-semibold text-white">
                    Ends in {countdown}
                  </div>
                )}
              </div>
            </div>
          )}

          <div className="flex flex-wrap items-center justify-between gap-3">
            <div />
            <div className="flex items-center gap-2">
              <span className="text-xs text-muted-foreground">Sort by</span>
              <select
                aria-label="Sort products"
                title="Sort products"
                className="h-9 rounded-md border bg-background px-3 text-sm shadow-sm"
                value={sortKey}
                onChange={(e) => setSortKey(e.target.value)}
              >
                <option value="best-match">Best match</option>
                <option value="sold">Best sellers</option>
                <option value="rating">Top rated</option>
                <option value="price-low">Price: low to high</option>
                <option value="price-high">Price: high to low</option>
              </select>
            </div>
          </div>
          {isBusy ? (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
              {Array.from({ length: 9 }).map((_, i) => (
                <LoadingCard key={i} />
              ))}
            </div>
          ) : error ? (
            <EmptyState title="Could not load products" description={error} />
          ) : pagedProducts.length === 0 ? (
            <EmptyState title="No products found" description="Try adjusting your filters." />
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
                {pagedProducts.map((p, index) => {
                  const id = getNumber(p, "id") ?? index + 1;
                  return <ProductCard key={String(id)} product={p} href={`/products/${id}`} />;
                })}
              </div>
              <div className="flex items-center justify-end gap-2">
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    const next = new URLSearchParams(searchParams);
                    next.set("page", String(Math.max(0, page - 1)));
                    setSearchParams(next, { replace: true });
                  }}
                  disabled={isBusy || page <= 0}
                >
                  Prev
                </Button>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    const next = new URLSearchParams(searchParams);
                    next.set("page", String(page + 1));
                    setSearchParams(next, { replace: true });
                  }}
                  disabled={isNextDisabled}
                >
                  Next
                </Button>
              </div>
            </div>
          )
        }
        </div>
      </div>
    </div>
  );
}

