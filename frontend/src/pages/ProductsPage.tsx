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

type ProductSummary = unknown;
type CategorySummary = unknown;

export default function ProductsPage() {
  const [searchParams, setSearchParams] = useSearchParams();

  const page = Number(searchParams.get("page") ?? "0");
  const size = Number(searchParams.get("size") ?? "12");
  const name = searchParams.get("name") ?? "";
  const categoryId = searchParams.get("categoryId") ?? "";

  const [data, setData] = useState<SpringPage<ProductSummary> | null>(null);
  const [categories, setCategories] = useState<CategorySummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

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
        const [result, categoryPage] = await Promise.all([
          apiGet<SpringPage<ProductSummary>>(`/api/public/products${queryString}`),
          apiGet<SpringPage<CategorySummary>>(
            `/api/public/categories${buildQuery({ page: 0, size: 50 })}`
          ),
        ]);
        if (!isMounted) return;
        setData(result);
        setCategories(categoryPage?.content ?? []);
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
  }, [queryString]);

  const products = data?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-2 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Browse</div>
          <h1 className="text-2xl font-bold tracking-tight">Products</h1>
          <div className="mt-1 text-xs text-muted-foreground">
            Find items fast with search, category chips, and pagination.
          </div>
        </div>

        <div className="flex items-center gap-2">
          <Button
            type="button"
            variant="outline"
            onClick={() => {
              const next = new URLSearchParams(searchParams);
              next.set("page", String(Math.max(0, page - 1)));
              setSearchParams(next, { replace: true });
            }}
            disabled={isLoading || page <= 0}
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
            disabled={isLoading || Boolean(data?.last)}
          >
            Next
          </Button>
        </div>
      </div>

      <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
        <Card className="h-fit shine">
          <CardHeader>
            <CardTitle className="text-base">Filters</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <div className="text-xs font-medium text-muted-foreground">Search</div>
              <Input
                placeholder="Product name..."
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
              <div className="text-xs font-medium text-muted-foreground">Category</div>
              <div className="flex flex-wrap gap-2">
                <button
                  type="button"
                  className={`pressable rounded-full border px-3 py-1 text-xs shadow-sm transition hover:-translate-y-0.5 ${
                    categoryId ? "bg-background" : "border-primary bg-primary text-primary-foreground"
                  }`}
                  onClick={() => {
                    const next = new URLSearchParams(searchParams);
                    next.delete("categoryId");
                    next.set("page", "0");
                    setSearchParams(next, { replace: true });
                  }}
                >
                  All
                </button>
                {categories.map((c, index) => {
                  const id = String(getNumber(c, "id") ?? index + 1);
                  const label = getString(c, "name", "title") ?? `Category ${id}`;
                  const active = categoryId === id;
                  return (
                    <button
                      key={id}
                      type="button"
                      className={`pressable rounded-full border px-3 py-1 text-xs shadow-sm transition hover:-translate-y-0.5 ${
                        active
                          ? "border-primary bg-primary text-primary-foreground"
                          : "bg-background hover:bg-muted"
                      }`}
                      onClick={() => {
                        const next = new URLSearchParams(searchParams);
                        next.set("categoryId", id);
                        next.set("page", "0");
                        setSearchParams(next, { replace: true });
                      }}
                      title={label}
                    >
                      {label}
                    </button>
                  );
                })}
              </div>
            </div>
          </CardContent>
        </Card>

        <div className="space-y-4">
          {isLoading ? (
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {Array.from({ length: 9 }).map((_, i) => (
                <LoadingCard key={i} />
              ))}
            </div>
          ) : error ? (
            <EmptyState title="Failed to load products" description={error} />
          ) : products.length === 0 ? (
            <EmptyState title="No products" description="Try adjusting your filters." />
          ) : (
            <div className="space-y-4">
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                {products.map((p, index) => {
                  const id = getNumber(p, "id") ?? index + 1;
                  return <ProductCard key={String(id)} product={p} href={`/products/${id}`} />;
                })}
              </div>
              <div className="flex items-center justify-between gap-2">
                <div className="text-xs text-muted-foreground">
                  Page {page + 1} / {data?.totalPages ?? 1} • {data?.totalElements ?? products.length} items
                </div>
                <div className="flex items-center gap-2">
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => {
                      const next = new URLSearchParams(searchParams);
                      next.set("page", String(Math.max(0, page - 1)));
                      setSearchParams(next, { replace: true });
                    }}
                    disabled={isLoading || page <= 0}
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
                    disabled={isLoading || Boolean(data?.last)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
