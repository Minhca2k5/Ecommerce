import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import ProductCard from "@/components/ProductCard";
import CategoryIcon from "@/components/CategoryIcon";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGet, buildQuery } from "@/lib/apiClient";
import type { SpringPage } from "@/lib/pagination";
import { getNumber, getString } from "@/lib/safe";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import { Link, useParams } from "react-router-dom";
import { useEffect, useMemo, useState } from "react";

type Category = unknown;
type Product = unknown;

export default function CategoryDetailPage() {
  const { categoryId, slug } = useParams();
  const categoryIdNumber = useMemo(() => Number(categoryId), [categoryId]);
  const slugValue = useMemo(() => (slug ?? "").trim(), [slug]);
  const isSlugRoute = useMemo(() => slugValue.length > 0, [slugValue]);

  const [category, setCategory] = useState<Category | null>(null);
  const [subcategories, setSubcategories] = useState<Category[]>([]);
  const [productPage, setProductPage] = useState<SpringPage<Product> | null>(null);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(9);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      if (!isSlugRoute && (!Number.isFinite(categoryIdNumber) || categoryIdNumber <= 0)) {
        setError("Category not found");
        setIsLoading(false);
        return;
      }

      try {
        setIsLoading(true);
        setError(null);

        const categoryEndpoint = isSlugRoute
          ? `/api/public/categories/slug/${encodeURIComponent(slugValue)}/details`
          : `/api/public/categories/${categoryIdNumber}/details`;

        let c = await apiGet<Category>(categoryEndpoint);
        let resolvedCategoryId = getNumber(c, "id") ?? categoryIdNumber;

        if ((!resolvedCategoryId || resolvedCategoryId <= 0) && isSlugRoute) {
          const resolved = await apiGet<{
            content?: unknown[];
          }>(`/api/public/categories?slug=${encodeURIComponent(slugValue)}&page=0&size=1`);
          const first = resolved?.content?.[0];
          const fallbackId = getNumber(first, "id");
          if (fallbackId) {
            c = await apiGet<Category>(`/api/public/categories/${fallbackId}/details`);
            resolvedCategoryId = fallbackId;
          }
        }

        const [subs, pageData] = await Promise.all([
          apiGet<Category[]>(`/api/public/categories/${resolvedCategoryId}/subcategories`),
          apiGet<SpringPage<Product>>(
            `/api/public/products${buildQuery({
              page,
              size,
              categoryId: resolvedCategoryId,
            })}`
          ),
        ]);
        if (!isMounted) return;
        setCategory(c);
        setSubcategories(subs ?? []);
        setProductPage(pageData ?? null);
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
  }, [categoryIdNumber, isSlugRoute, slugValue, page, size]);

  const title = getString(category, "name", "title") ?? `Category #${categoryIdNumber}`;
  const resolvedCategoryId = getNumber(category, "id") ?? categoryIdNumber;
  const products = productPage?.content ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-3">
        <div>
          <div className="text-sm text-muted-foreground">Category</div>
          <h1 className="text-2xl font-bold tracking-tight">{title}</h1>
          <div className="mt-1 text-xs text-muted-foreground">
            Browse subcategories, then paginate products.
          </div>
        </div>
        <Button asChild variant="outline">
          <Link to="/products">Browse products</Link>
        </Button>
      </div>

      {isLoading ? (
        <div className="grid gap-4 lg:grid-cols-2">
          <LoadingCard />
          <LoadingCard />
        </div>
      ) : error ? (
        <EmptyState title="Failed to load category" description={error} />
      ) : (
        <div className="grid gap-6 lg:grid-cols-[320px_1fr]">
          <div className="space-y-4">
            <Card>
              <CardHeader>
                <CardTitle className="text-base">Subcategories</CardTitle>
              </CardHeader>
              <CardContent className="flex flex-wrap gap-2">
                {subcategories.length === 0 ? (
                  <div className="text-sm text-muted-foreground">No subcategories</div>
                ) : (
                  subcategories.map((s, idx) => {
                    const id = getNumber(s, "id") ?? idx + 1;
                    const name = getString(s, "name", "title") ?? `Category #${id}`;
                    const slug = getString(s, "slug");
                    const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
                    const href = `/categories/${id}`;
                    return (
                      <Link
                        key={String(id)}
                        to={href}
                        className="shine pressable group inline-flex items-center gap-2 rounded-full border bg-background/70 px-3 py-1 text-xs shadow-sm backdrop-blur transition hover:-translate-y-0.5 hover:bg-muted hover:shadow-md"
                      >
                        <CategoryIcon
                          name={meta.icon}
                          className="h-4 w-4 transition duration-300 group-hover:-rotate-6"
                        />
                        <span className="font-medium">{name}</span>
                      </Link>
                    );
                  })
                )}
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-base">Quick actions</CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <Button asChild className="w-full justify-start" variant="secondary">
                  <Link to={`/products?categoryId=${resolvedCategoryId}`}>
                    <span className="inline-flex items-center gap-2">
                      <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M4 6h16" />
                        <path d="M6 12h12" />
                        <path d="M10 18h4" />
                      </svg>
                      Filter products by this category
                    </span>
                  </Link>
                </Button>
                <Button asChild className="w-full justify-start" variant="outline">
                  <Link to="/categories">
                    <span className="inline-flex items-center gap-2">
                      <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M4 6h7v7H4z" />
                        <path d="M13 6h7v7h-7z" />
                        <path d="M4 15h7v5H4z" />
                        <path d="M13 15h7v5h-7z" />
                      </svg>
                      All categories
                    </span>
                  </Link>
                </Button>
              </CardContent>
            </Card>
          </div>

          <div className="space-y-3">
            <div className="flex items-center justify-between">
              <h2 className="text-lg font-semibold tracking-tight">Products</h2>
              <div className="flex items-center gap-2">
                <div className="text-xs text-muted-foreground">Show:</div>
                <select
                  className="h-9 cursor-pointer rounded-md border bg-background px-3 text-sm shadow-sm transition hover:bg-muted"
                  value={String(size)}
                  onChange={(e) => {
                    const next = Number(e.target.value);
                    setSize(Number.isFinite(next) ? next : 9);
                    setPage(0);
                  }}
                >
                  <option value="6">6</option>
                  <option value="9">9</option>
                  <option value="12">12</option>
                  <option value="18">18</option>
                </select>
                <Button asChild variant="ghost">
                  <Link to={`/products?categoryId=${resolvedCategoryId}`}>View all</Link>
                </Button>
              </div>
            </div>

            {products.length === 0 ? (
              <EmptyState title="No products" description="This category has no products yet." />
            ) : (
              <div className="space-y-4">
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                  {products.map((p, idx) => {
                    const id = getNumber(p, "id") ?? idx + 1;
                    return <ProductCard key={String(id)} product={p} href={`/products/${id}`} />;
                  })}
                </div>
                <div className="flex items-center justify-between gap-2">
                  <div className="text-xs text-muted-foreground">
                    Page {Math.min(page + 1, (productPage?.totalPages ?? 1) || 1)} /{" "}
                    {productPage?.totalPages ?? 1}
                  </div>
                  <div className="flex items-center gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      disabled={isLoading || page <= 0}
                      onClick={() => setPage((p) => Math.max(0, p - 1))}
                    >
                      Prev
                    </Button>
                    <Button
                      type="button"
                      variant="outline"
                      disabled={isLoading || Boolean(productPage?.last)}
                      onClick={() => setPage((p) => p + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
