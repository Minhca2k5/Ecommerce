import EmptyState from "@/components/EmptyState";
import CategoryIcon from "@/components/CategoryIcon";
import LoadingCard from "@/components/LoadingCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGet, buildQuery } from "@/lib/apiClient";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import type { SpringPage } from "@/lib/pagination";
import { getNumber, getString } from "@/lib/safe";
import { getErrorMessage } from "@/lib/errors";
import { Link } from "react-router-dom";
import { useEffect, useState } from "react";

type CategorySummary = unknown;

export default function CategoriesPage() {
  const [data, setData] = useState<SpringPage<CategorySummary> | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setIsLoading(true);
        setError(null);
        const page = await apiGet<SpringPage<CategorySummary>>(
          `/api/public/categories${buildQuery({ page: 0, size: 50 })}`
        );
        if (!isMounted) return;
        setData(page);
      } catch (e) {
        if (!isMounted) return;
        setError(getErrorMessage(e, "Failed to load categories."));
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

  const categories = data?.content ?? [];

  return (
    <div className="space-y-8">
      <section className="relative overflow-hidden rounded-2xl border border-primary/15 bg-white/85 p-8 shadow-sm">
        <div className="pointer-events-none absolute -left-10 -top-16 h-48 w-48 rounded-full bg-primary/10 blur-3xl" />
        <div className="pointer-events-none absolute -right-6 top-6 h-40 w-40 rounded-full bg-amber-400/15 blur-3xl" />
        <div className="relative z-10 max-w-2xl space-y-4">
          <span className="inline-flex w-fit items-center gap-2 rounded-full border border-primary/20 bg-white/80 px-3 py-1 text-xs font-semibold uppercase tracking-[0.2em] text-primary">
            Collections
          </span>
          <div className="space-y-2">
            <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Browse by category</h1>
            <p className="text-base text-muted-foreground">
              Curated lanes for every shopping mood. Dive into a category to see what&apos;s trending and ready to ship.
            </p>
          </div>
          <div className="flex flex-wrap gap-3">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 rounded-xl border border-primary/20 bg-primary/10 px-4 py-2 text-sm font-semibold text-primary transition hover:bg-primary hover:text-primary-foreground"
            >
              Explore all products
              <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M5 12h14" />
                <path d="M13 6l6 6-6 6" />
              </svg>
            </Link>
            <div className="inline-flex items-center gap-2 rounded-xl border border-primary/15 bg-white/70 px-4 py-2 text-sm text-muted-foreground">
              Updated daily with new arrivals
            </div>
          </div>
        </div>
      </section>

      {isLoading ? (
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {Array.from({ length: 6 }).map((_, i) => (
            <LoadingCard key={i} />
          ))}
        </div>
      ) : error ? (
        <EmptyState title="Failed to load categories" description={error} />
      ) : categories.length === 0 ? (
        <EmptyState title="No categories" description="Try again later." />
      ) : (
        <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((c) => {
            const id = getNumber(c, "id") ?? 0;
            const name = getString(c, "name", "title") ?? "Category";
            const slug = getString(c, "slug");
            const href = `/categories/${id}`;
            const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
            return (
              <Link key={id} to={href} className="block cursor-pointer">
                <Card className="category-card pressable group relative overflow-hidden border transition">
                  <div className={`absolute inset-0 ${meta.gradientClassName}`} />
                  <div className="absolute -right-10 -top-16 h-32 w-32 rounded-full bg-white/60 blur-2xl" />
                  <CardHeader className="relative z-10 space-y-4 pb-2">
                    <div className="flex items-start justify-between gap-3">
                      <CategoryIcon
                        name={meta.icon}
                        className="border-white/60 bg-white/85 shadow-md group-hover:-translate-y-0.5 transition"
                      />
                    </div>
                    <div className="space-y-2">
                      <CardTitle className="text-lg">{name}</CardTitle>
                      <div className="text-sm text-muted-foreground">
                        {meta.description}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="relative z-10 pt-2">
                    <div className="inline-flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-semibold text-primary transition group-hover:text-primary-foreground category-cta group-hover:bg-primary">
                      Explore category
                      <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M5 12h14" />
                        <path d="M13 6l6 6-6 6" />
                      </svg>
                    </div>
                  </CardContent>
                </Card>
              </Link>
            );
          })}
        </div>
      )}
    </div>
  );
}
