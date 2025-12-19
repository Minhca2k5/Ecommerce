import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import ProductCard from "@/components/ProductCard";
import SafeImage from "@/components/SafeImage";
import { apiGet, buildQuery } from "@/lib/apiClient";
import { getNumber, getString } from "@/lib/safe";
import { getAppBaseUrl, isAbsoluteUrl } from "@/lib/env";
import { Link } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import CategoryIcon from "@/components/CategoryIcon";
import { Button } from "@/components/ui/button";

type Banner = unknown;
type Product = unknown;
type Category = unknown;

const topListOptions = [
  { key: "top-rating", label: "Top rating" },
  { key: "most-favorite", label: "Most favorite" },
  { key: "most-viewed", label: "Most viewed" },
  { key: "best-selling", label: "Best selling" },
] as const;

export default function HomePage() {
  const [home, setHome] = useState<unknown>(null);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [homeCategories, setHomeCategories] = useState<Category[]>([]);
  const [topKey, setTopKey] =
    useState<(typeof topListOptions)[number]["key"]>("top-rating");
  const [topProducts, setTopProducts] = useState<Product[]>([]);
  const [topPage, setTopPage] = useState(0);
  const [topPageSize, setTopPageSize] = useState(9);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const topEndpoint = useMemo(() => `/api/public/products/${topKey}`, [topKey]);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setIsLoading(true);
        setError(null);

        const data = await apiGet<unknown>(`/api/public/home${buildQuery({})}`);

        if (!isMounted) return;
        setHome(data);
        setBanners((data as any)?.banners ?? []);
        setHomeCategories((data as any)?.categories ?? []);
      } catch (e) {
        if (!isMounted) return;
        setError(e instanceof Error ? e.message : "Unknown error");
      } finally {
        if (!isMounted) return;
        setIsLoading(false);
      }
    }

    void run();
    return () => {
      isMounted = false;
    };
  }, []);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setError(null);

        const listFromHome =
          topKey === "top-rating"
            ? (home as any)?.topRated
            : topKey === "most-viewed"
              ? (home as any)?.mostViewed
              : topKey === "best-selling"
                ? (home as any)?.bestSellers
                : null;

        if (Array.isArray(listFromHome)) {
          setTopProducts(listFromHome);
          return;
        }

        const top = await apiGet<Product[]>(topEndpoint);
        if (!isMounted) return;
        setTopProducts(top ?? []);
      } catch (e) {
        if (!isMounted) return;
        setError(e instanceof Error ? e.message : "Unknown error");
      }
    }

    if (!home) return;
    void run();
    return () => {
      isMounted = false;
    };
  }, [home, topEndpoint, topKey]);

  useEffect(() => {
    setTopPage(0);
  }, [topKey]);

  const topTotal = topProducts.length;
  const topTotalPages = Math.max(1, Math.ceil(topTotal / topPageSize));
  const topPageClamped = Math.min(topPage, topTotalPages - 1);
  const topStart = topPageClamped * topPageSize;
  const topEnd = topStart + topPageSize;
  const topPageItems = topProducts.slice(topStart, topEnd);

  return (
    <div className="space-y-8">
      <section className="shine rounded-2xl border bg-gradient-to-br from-primary/30 via-background to-background p-6">
        <div className="max-w-2xl space-y-2">
          <div className="text-sm text-muted-foreground">Storefront</div>
          <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">
            Discover products you'll love
          </h1>
          <p className="text-sm text-muted-foreground">
            Browse trending items, explore categories, and check product details — powered by your
            backend APIs.
          </p>
        </div>
      </section>

      {isLoading ? (
        <div className="grid gap-4 lg:grid-cols-2">
          <LoadingCard />
          <LoadingCard />
        </div>
      ) : error ? (
        <EmptyState title="Failed to load homepage" description={error} />
      ) : (
        <>
          {homeCategories.length ? (
            <section className="space-y-3">
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold tracking-tight">Popular categories</h2>
                <Link
                  to="/categories"
                  className="text-sm text-primary underline-offset-4 hover:underline"
                >
                  View all
                </Link>
              </div>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {homeCategories.slice(0, 8).map((c, idx) => {
                  const id = getNumber(c, "id") ?? idx + 1;
                  const name = getString(c, "name") ?? `Category #${id}`;
                  const slug = getString(c, "slug");
                  const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
                  return (
                    <Link
                      key={String(id)}
                      to={`/categories/${id}`}
                      className="shine pressable group flex items-center gap-3 rounded-2xl border bg-card/80 p-3 backdrop-blur transition hover:-translate-y-0.5 hover:shadow-lg"
                    >
                      <CategoryIcon
                        name={meta.icon}
                        className="transition duration-300 group-hover:-rotate-3 group-hover:scale-105"
                      />
                      <div className="min-w-0">
                        <div className="truncate text-sm font-semibold">{name}</div>
                        <div className="truncate text-xs text-muted-foreground">
                          {meta.description}
                        </div>
                      </div>
                    </Link>
                  );
                })}
              </div>
            </section>
          ) : null}

          <section className="space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold tracking-tight">Banners</h2>
              <a
                href="/products"
                className="text-sm text-primary underline-offset-4 hover:underline"
              >
                Browse products
              </a>
            </div>

            {banners.length ? (
              <div className="flex gap-3 overflow-auto pb-2">
                {banners.map((b, index) => {
                  const title = getString(b, "title") ?? `Banner #${index + 1}`;
                  const imageUrl = getString(b, "imageUrl", "image", "url");
                  const targetPath = getString(b, "targetUrl", "targetPath", "url") ?? "/products";
                  const targetUrl = isAbsoluteUrl(targetPath)
                    ? targetPath
                    : `${getAppBaseUrl()}${targetPath.startsWith("/") ? "" : "/"}${targetPath}`;

                  const isInternal = !isAbsoluteUrl(targetPath) && targetPath.startsWith("/");

                  return (
                    <div
                      key={String(index)}
                      className="group relative min-w-[320px] overflow-hidden rounded-xl border bg-card shadow-sm"
                    >
                      {isInternal ? (
                        <Link to={targetPath} className="block">
                          <div className="aspect-[16/7] bg-gradient-to-br from-primary/20 via-background to-background">
                            <SafeImage
                              src={imageUrl}
                              alt={title}
                              fallbackKey={`banner-${index}`}
                              className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.02]"
                            />
                          </div>
                          <div className="p-4">
                            <div className="line-clamp-1 text-sm font-semibold">{title}</div>
                            <div className="mt-1 text-xs text-muted-foreground">
                              View collection
                            </div>
                          </div>
                        </Link>
                      ) : (
                        <a href={targetUrl} className="block">
                          <div className="aspect-[16/7] bg-gradient-to-br from-primary/20 via-background to-background">
                            <SafeImage
                              src={imageUrl}
                              alt={title}
                              fallbackKey={`banner-${index}`}
                              className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.02]"
                            />
                          </div>
                          <div className="p-4">
                            <div className="line-clamp-1 text-sm font-semibold">{title}</div>
                            <div className="mt-1 text-xs text-muted-foreground">
                              View collection
                            </div>
                          </div>
                        </a>
                      )}
                    </div>
                  );
                })}
              </div>
            ) : (
              <div className="text-sm text-muted-foreground">No banners</div>
            )}
          </section>

          <section className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <h2 className="text-lg font-semibold tracking-tight">Top products</h2>
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                <div className="flex items-center gap-2">
                  <div className="text-xs text-muted-foreground">List:</div>
                  <select
                    className="h-9 cursor-pointer rounded-md border bg-background px-3 text-sm shadow-sm transition hover:bg-muted"
                    value={topKey}
                    onChange={(e) => setTopKey(e.target.value as typeof topKey)}
                  >
                    {topListOptions.map((opt) => (
                      <option key={opt.key} value={opt.key}>
                        {opt.label}
                      </option>
                    ))}
                  </select>
                </div>

                <div className="flex items-center gap-2">
                  <div className="text-xs text-muted-foreground">Show:</div>
                  <select
                    className="h-9 cursor-pointer rounded-md border bg-background px-3 text-sm shadow-sm transition hover:bg-muted"
                    value={String(topPageSize)}
                    onChange={(e) => {
                      const next = Number(e.target.value);
                      setTopPageSize(Number.isFinite(next) ? next : 9);
                      setTopPage(0);
                    }}
                  >
                    <option value="6">6</option>
                    <option value="9">9</option>
                    <option value="12">12</option>
                    <option value="18">18</option>
                  </select>
                </div>
              </div>
            </div>

            {topProducts.length ? (
              <div className="space-y-4">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {topPageItems.map((p, index) => {
                  const id = getNumber(p, "id") ?? index + 1;
                  return <ProductCard key={String(id)} product={p} href={`/products/${id}`} />;
                  })}
                </div>

                <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div className="text-xs text-muted-foreground">
                    Showing {Math.min(topStart + 1, topTotal)}-{Math.min(topEnd, topTotal)} of{" "}
                    {topTotal}
                  </div>
                  <div className="flex items-center justify-end gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      disabled={topPageClamped <= 0}
                      onClick={() => setTopPage((p) => Math.max(0, p - 1))}
                    >
                      Prev
                    </Button>
                    <div className="rounded-full border bg-background/70 px-3 py-1 text-xs text-muted-foreground backdrop-blur">
                      Page {topPageClamped + 1} / {topTotalPages}
                    </div>
                    <Button
                      type="button"
                      variant="outline"
                      disabled={topPageClamped >= topTotalPages - 1}
                      onClick={() => setTopPage((p) => Math.min(topTotalPages - 1, p + 1))}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            ) : (
              <EmptyState title="No products" description="Try another top list." />
            )}
          </section>
        </>
      )}
    </div>
  );
}
