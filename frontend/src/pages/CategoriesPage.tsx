import EmptyState from "@/components/EmptyState";
import CategoryIcon from "@/components/CategoryIcon";
import LoadingCard from "@/components/LoadingCard";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGet, buildQuery } from "@/lib/apiClient";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import type { SpringPage } from "@/lib/pagination";
import { getNumber, getString } from "@/lib/safe";
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

  const categories = data?.content ?? [];

  return (
    <div className="space-y-6">
      <section className="rounded-2xl border bg-gradient-to-br from-primary/20 via-background to-background p-6">
        <div className="max-w-2xl space-y-2">
          <div className="text-sm text-muted-foreground">Browse</div>
          <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">Categories</h1>
          <p className="text-sm text-muted-foreground">
            Explore curated collections—pick a category and discover what’s trending.
          </p>
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
        <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {categories.map((c) => {
            const id = getNumber(c, "id") ?? 0;
            const name = getString(c, "name", "title") ?? `Category #${id}`;
            const slug = getString(c, "slug");
            const href = `/categories/${id}`;
            const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
            return (
              <Link key={id} to={href} className="block cursor-pointer">
                <Card className="shine pressable group overflow-hidden border-muted/60 bg-card/80 backdrop-blur transition hover:-translate-y-0.5 hover:shadow-lg">
                  <div className={`h-20 bg-gradient-to-br ${meta.gradientClassName}`} />
                  <CardHeader className="-mt-10 space-y-3">
                    <div className="flex items-start justify-between gap-3">
                      <div className="flex items-center gap-3">
                        <CategoryIcon
                          name={meta.icon}
                          className="transition duration-300 group-hover:-rotate-3 group-hover:scale-105"
                        />
                        <div className="space-y-1">
                          <CardTitle className="text-base">{name}</CardTitle>
                          <div className="text-xs text-muted-foreground">
                            {meta.description}
                          </div>
                        </div>
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-0">
                    <div className="inline-flex items-center gap-2 rounded-xl border bg-background/70 px-3 py-2 text-sm font-medium backdrop-blur transition hover:bg-accent">
                      Explore
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
