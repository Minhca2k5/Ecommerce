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

function parsePriceInput(value: string) {
  const normalized = value.replace(/,/g, "").trim();
  if (!normalized) return null;
  const parsed = Number(normalized);
  return Number.isFinite(parsed) && parsed >= 0 ? parsed : null;
}

function getPriceSortIndicator(sortKey: string) {
  if (sortKey === "price-low") return "up";
  if (sortKey === "price-high") return "down";
  return "";
}

export default function ProductsPage() {
  const auth = useAuth();
  const toast = useToast();
  const [searchParams, setSearchParams] = useSearchParams();

  const page = Number(searchParams.get("page") ?? "0");
  const size = Number(searchParams.get("size") ?? "12");
  const name = searchParams.get("name") ?? "";
  const categoryId = searchParams.get("categoryId") ?? "";
  const minPrice = searchParams.get("minPrice") ?? "";
  const maxPrice = searchParams.get("maxPrice") ?? "";
  const warehouseLocation = searchParams.get("warehouseLocation") ?? "";

  const [data, setData] = useState<SpringPage<ProductSummary> | null>(null);
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [warehouseLocations, setWarehouseLocations] = useState<string[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [hasNewProducts, setHasNewProducts] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);
  const [sortKey, setSortKey] = useState("best-match");
  const [flashSale, setFlashSale] = useState<FlashSaleSummary | null>(null);
  const [dealFilters, setDealFilters] = useState({
    topRated: false,
    bestSeller: false,
  });
  const [topRatedDealIds, setTopRatedDealIds] = useState<Set<number>>(new Set());
  const [bestSellerDealIds, setBestSellerDealIds] = useState<Set<number>>(new Set());
  const [nowTs, setNowTs] = useState(() => Date.now());
  const [priceDraft, setPriceDraft] = useState({ min: minPrice, max: maxPrice });
  const hasDealFilter = dealFilters.topRated || dealFilters.bestSeller;
  const requestPage = hasDealFilter ? 0 : page;
  const requestSize = hasDealFilter ? 240 : size;
  const requestSortBy =
    sortKey === "price-low" || sortKey === "price-high"
      ? "salePrice"
      : sortKey === "rating"
        ? "recentlyAverageRating"
        : sortKey === "sold"
          ? "recentlyTotalSoldQuantity"
          : undefined;
  const requestSortDirection =
    sortKey === "price-low"
      ? "asc"
      : sortKey === "price-high" || sortKey === "rating" || sortKey === "sold"
        ? "desc"
        : undefined;
  const parsedMinPrice = parsePriceInput(minPrice);
  const parsedMaxPrice = parsePriceInput(maxPrice);
  const normalizedMinPrice =
    parsedMinPrice !== null && parsedMaxPrice !== null
      ? Math.min(parsedMinPrice, parsedMaxPrice)
      : parsedMinPrice;
  const normalizedMaxPrice =
    parsedMinPrice !== null && parsedMaxPrice !== null
      ? Math.max(parsedMinPrice, parsedMaxPrice)
      : parsedMaxPrice;

  const queryString = useMemo(
    () =>
      buildQuery({
        page: requestPage,
        size: requestSize,
        name: name.trim() ? name.trim() : undefined,
        categoryId: categoryId.trim() ? categoryId.trim() : undefined,
        minPrice: normalizedMinPrice !== null ? String(normalizedMinPrice) : undefined,
        maxPrice: normalizedMaxPrice !== null ? String(normalizedMaxPrice) : undefined,
        warehouseLocation: warehouseLocation.trim() ? warehouseLocation.trim() : undefined,
        sortBy: requestSortBy,
        sortDirection: requestSortDirection,
      }),
    [requestPage, requestSize, name, categoryId, normalizedMinPrice, normalizedMaxPrice, warehouseLocation, requestSortBy, requestSortDirection]
  );

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setIsLoading(true);
        setError(null);
        const [result, categoryPage, locationRows, flashSaleData] = await Promise.all([
          apiGet<SpringPage<ProductSummary>>(`/api/public/products${queryString}`),
          apiGet<SpringPage<CategorySummary>>(
            `/api/public/categories${buildQuery({ page: 0, size: 50 })}`
          ),
          apiGet<string[]>(`/api/public/warehouses/locations`),
          apiGet<FlashSaleSummary>(`/api/public/products/flash-sale`),
        ]);
        if (!isMounted) return;
        setData(result);
        setCategories(categoryPage?.content ?? []);
        setWarehouseLocations(Array.isArray(locationRows) ? locationRows : []);
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
    setPriceDraft({ min: minPrice, max: maxPrice });
  }, [minPrice, maxPrice]);

  useEffect(() => {
    const id = window.setInterval(() => setNowTs(Date.now()), 1000);
    return () => window.clearInterval(id);
  }, []);

  useEffect(() => {
    let alive = true;
    Promise.all([
      apiGet<ProductSummary[]>("/api/public/products/top-rating"),
      apiGet<ProductSummary[]>("/api/public/products/best-selling"),
    ])
      .then(([topRatedRows, bestSellerRows]) => {
        if (!alive) return;
        const topIds = new Set<number>((topRatedRows ?? []).map((p) => getNumber(p, "id") ?? -1).filter((id) => id > 0));
        const bestIds = new Set<number>((bestSellerRows ?? []).map((p) => getNumber(p, "id") ?? -1).filter((id) => id > 0));
        setTopRatedDealIds(topIds);
        setBestSellerDealIds(bestIds);
      })
      .catch(() => {
        if (!alive) return;
        setTopRatedDealIds(new Set());
        setBestSellerDealIds(new Set());
      });
    return () => {
      alive = false;
    };
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
    }, 700);
    return () => window.clearTimeout(handle);
  }, [auth.isAuthenticated, name]);

  useEffect(() => {
    const next = new URLSearchParams(searchParams);
    if (next.get("page") !== "0") {
      next.set("page", "0");
      setSearchParams(next, { replace: true });
    }
  }, [dealFilters, searchParams, setSearchParams]);

  const products = data?.content ?? [];

  const filteredProducts = useMemo(() => {
    if (!dealFilters.topRated && !dealFilters.bestSeller) return products;
    return products.filter((p) => {
      const id = getNumber(p, "id") ?? -1;
      const rating = getNumber(p, "recentlyAverageRating", "averageRating", "rating") ?? 0;
      const reviewCount = getNumber(p, "recentlyReviewCount", "reviewCount", "totalReviews") ?? 0;
      const sold = getNumber(p, "recentlyTotalSoldQuantity", "totalSold", "soldCount") ?? 0;
      const topRated = topRatedDealIds.size > 0
        ? topRatedDealIds.has(id)
        : (rating >= 4.0 && reviewCount >= 1) || rating >= 4.5;
      const bestSeller = bestSellerDealIds.size > 0
        ? bestSellerDealIds.has(id)
        : sold >= 5;
      if (dealFilters.topRated && !topRated) return false;
      if (dealFilters.bestSeller && !bestSeller) return false;
      return true;
    });
  }, [dealFilters, products, topRatedDealIds, bestSellerDealIds]);

  const sortedProducts = useMemo(() => {
    if (sortKey === "price-low" || sortKey === "price-high" || sortKey === "rating" || sortKey === "sold") {
      return filteredProducts;
    }
    return filteredProducts;
  }, [filteredProducts, sortKey]);

  const useClientPagination = hasDealFilter;
  const totalPagesForClient = Math.max(1, Math.ceil(sortedProducts.length / Math.max(1, size)));
  const pagedProducts = useMemo(() => {
    if (!useClientPagination) return sortedProducts;
    const start = page * size;
    return sortedProducts.slice(start, start + size);
  }, [useClientPagination, page, size, sortedProducts]);

  const isBusy = isLoading;
  const isNextDisabled = isBusy || (useClientPagination ? page + 1 >= totalPagesForClient : Boolean(data?.last));
  const totalPagesDisplay = useClientPagination ? totalPagesForClient : Math.max(1, Number(data?.totalPages ?? 1));
  const currentPageDisplay = Math.min(Math.max(1, page + 1), totalPagesDisplay);

  function setSort(nextSort: string) {
    if (nextSort !== sortKey) {
      const next = new URLSearchParams(searchParams);
      next.set("page", "0");
      setSearchParams(next, { replace: true });
    }
    setSortKey(nextSort);
  }

  function togglePriceSort() {
    setSortKey((prev) => {
      if (prev === "price-low") return "price-high";
      if (prev === "price-high") return "price-low";
      return "price-low";
    });
  }

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
  const flashSaleDescription = flashSale?.description?.trim() || (isFlashSaleEnded ? "The next promotion will be available soon." : "Hot deals on select items today.");
  const countdown = formatCountdown(remainingMs);

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h1 className="text-2xl font-semibold tracking-tight sm:text-3xl">Browse all products</h1>
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
                next.delete("minPrice");
                next.delete("maxPrice");
                next.delete("warehouseLocation");
                next.set("page", "0");
                setSearchParams(next, { replace: true });
              }}
              disabled={isLoading || (!name.trim() && !categoryId.trim() && !minPrice.trim() && !maxPrice.trim() && !warehouseLocation.trim())}
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

      <div className="grid gap-6 lg:grid-cols-[300px_1fr]">
        <Card className="h-fit rounded-xl border bg-background shadow-sm lg:sticky lg:top-24">
          <CardHeader className="pb-3">
            <CardTitle className="text-sm font-bold uppercase tracking-[0.16em] text-muted-foreground">Search filters</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4 pt-0">
            <div className="space-y-2 rounded-md border bg-card p-3">
              <div className="text-[11px] font-semibold uppercase tracking-[0.18em] text-muted-foreground">Keyword</div>
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

            <div className="space-y-2 rounded-md border bg-card p-3">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Categories</div>
              <ul className="divide-y text-sm">
                <li>
                  <label
                    className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-2 transition ${
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
                        className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-2 transition ${
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

            <div className="space-y-2 rounded-md border bg-card p-3">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Deals</div>
              <ul className="divide-y text-sm">
                <li>
                  <label className="flex cursor-pointer items-center gap-2 py-2">
                    <input
                      type="checkbox"
                      checked={dealFilters.topRated}
                      onChange={(e) => setDealFilters((prev) => ({ ...prev, topRated: e.target.checked }))}
                    />
                    <span>Top rated 4.0+</span>
                  </label>
                </li>
                <li>
                  <label className="flex cursor-pointer items-center gap-2 py-2">
                    <input
                      type="checkbox"
                      checked={dealFilters.bestSeller}
                      onChange={(e) => setDealFilters((prev) => ({ ...prev, bestSeller: e.target.checked }))}
                    />
                    <span>Best sellers (5+ sold)</span>
                  </label>
                </li>
              </ul>
            </div>

            <div className="space-y-2 rounded-md border bg-card p-3">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Price range</div>
              <div className="grid grid-cols-2 gap-2">
                <Input
                  placeholder="Min"
                  inputMode="decimal"
                  value={priceDraft.min}
                  onChange={(e) => setPriceDraft((prev) => ({ ...prev, min: e.target.value }))}
                />
                <Input
                  placeholder="Max"
                  inputMode="decimal"
                  value={priceDraft.max}
                  onChange={(e) => setPriceDraft((prev) => ({ ...prev, max: e.target.value }))}
                />
              </div>
              <Button
                type="button"
                variant="outline"
                className="h-9 w-full"
                onClick={() => {
                  const next = new URLSearchParams(searchParams);
                  const parsedDraftMin = parsePriceInput(priceDraft.min);
                  const parsedDraftMax = parsePriceInput(priceDraft.max);
                  const min =
                    parsedDraftMin !== null && parsedDraftMax !== null
                      ? Math.min(parsedDraftMin, parsedDraftMax)
                      : parsedDraftMin;
                  const max =
                    parsedDraftMin !== null && parsedDraftMax !== null
                      ? Math.max(parsedDraftMin, parsedDraftMax)
                      : parsedDraftMax;
                  if (min !== null) next.set("minPrice", String(min));
                  else next.delete("minPrice");
                  if (max !== null) next.set("maxPrice", String(max));
                  else next.delete("maxPrice");
                  next.set("page", "0");
                  setSearchParams(next, { replace: true });
                  setPriceDraft({
                    min: min !== null ? String(min) : "",
                    max: max !== null ? String(max) : "",
                  });
                }}
              >
                Apply price
              </Button>
            </div>

            <div className="space-y-2 rounded-md border bg-card p-3">
              <div className="text-xs font-semibold uppercase tracking-[0.2em] text-muted-foreground">Ship from</div>
              <ul className="max-h-48 divide-y overflow-auto pr-1 text-sm">
                <li>
                  <label
                    className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-2 transition ${
                      !warehouseLocation ? "bg-orange-50 text-orange-700" : "hover:bg-muted"
                    }`}
                  >
                    <input
                      type="radio"
                      className="accent-orange-500"
                      checked={!warehouseLocation}
                      onChange={() => {
                        const next = new URLSearchParams(searchParams);
                        next.delete("warehouseLocation");
                        next.set("page", "0");
                        setSearchParams(next, { replace: true });
                      }}
                    />
                    <span>All locations</span>
                  </label>
                </li>
                {warehouseLocations.map((location) => (
                  <li key={location}>
                    <label
                      className={`flex cursor-pointer items-center gap-2 rounded-md px-2 py-2 transition ${
                        warehouseLocation === location ? "bg-orange-50 text-orange-700" : "hover:bg-muted"
                      }`}
                    >
                      <input
                        type="radio"
                        className="accent-orange-500"
                        checked={warehouseLocation === location}
                        onChange={() => {
                          const next = new URLSearchParams(searchParams);
                          next.set("warehouseLocation", location);
                          next.set("page", "0");
                          setSearchParams(next, { replace: true });
                        }}
                      />
                      <span>{location}</span>
                    </label>
                  </li>
                ))}
              </ul>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-5">
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

          <div className="rounded-md border bg-card px-3 py-3 sm:px-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
              <div className="space-y-1">
                <div className="text-sm text-muted-foreground">
                  {name.trim()
                    ? `Search results for "${name.trim()}"`
                    : "Browse all products"}
                </div>
                <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                  <span className="rounded-md bg-muted px-2 py-1 font-semibold text-foreground">
                    {sortedProducts.length} products
                  </span>
                  {dealFilters.topRated ? (
                    <span className="rounded-md border border-sky-200 bg-sky-50 px-2 py-1 font-medium text-sky-700">Top rated</span>
                  ) : null}
                  {dealFilters.bestSeller ? (
                    <span className="rounded-md border border-amber-200 bg-amber-50 px-2 py-1 font-medium text-amber-700">Best sellers</span>
                  ) : null}
                </div>
              </div>
              <div className="flex flex-wrap items-center justify-end gap-2 rounded-md bg-muted/40 p-1.5">
                <div className="px-2 text-xs font-semibold uppercase tracking-[0.12em] text-muted-foreground">Sort by</div>
                <button
                  type="button"
                  className={`h-9 rounded-md border px-3 text-sm font-semibold transition ${sortKey === "best-match" ? "border-primary bg-primary text-primary-foreground shadow-sm" : "border-border bg-background hover:bg-white"}`}
                  onClick={() => setSort("best-match")}
                >
                  Relevant
                </button>
                <button
                  type="button"
                  className={`h-9 rounded-md border px-3 text-sm font-semibold transition ${sortKey === "rating" ? "border-primary bg-primary text-primary-foreground shadow-sm" : "border-border bg-background hover:bg-white"}`}
                  onClick={() => setSort("rating")}
                >
                  Top rated
                </button>
                <button
                  type="button"
                  className={`h-9 rounded-md border px-3 text-sm font-semibold transition ${sortKey === "sold" ? "border-primary bg-primary text-primary-foreground shadow-sm" : "border-border bg-background hover:bg-white"}`}
                  onClick={() => setSort("sold")}
                >
                  Best sellers
                </button>
                <button
                  type="button"
                  className={`h-9 rounded-md border px-3 text-sm font-semibold transition ${(sortKey === "price-low" || sortKey === "price-high") ? "border-primary bg-primary text-primary-foreground shadow-sm" : "border-border bg-background hover:bg-white"}`}
                  onClick={togglePriceSort}
                >
                  Price {getPriceSortIndicator(sortKey)}
                </button>
                <div className="ml-1 rounded-md border border-border bg-white px-2.5 py-1.5 text-sm font-semibold text-slate-600">
                  Page {currentPageDisplay}/{totalPagesDisplay}
                </div>
              </div>
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
