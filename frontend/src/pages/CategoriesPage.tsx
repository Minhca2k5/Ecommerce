import EmptyState from "@/components/EmptyState";
import CategoryIcon from "@/components/CategoryIcon";
import LoadingCard from "@/components/LoadingCard";
import SafeImage from "@/components/SafeImage";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGet, buildQuery } from "@/lib/apiClient";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import type { SpringPage } from "@/lib/pagination";
import { getNumber, getString } from "@/lib/safe";
import { getErrorMessage } from "@/lib/errors";
import { cn } from "@/lib/utils";
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
  const featured = categories.find((c) => getString(c, "slug") === "fashion") ?? categories[0];
  const featuredId = featured ? getNumber(featured, "id") : null;
  const featuredSlug = featured ? getString(featured, "slug") : null;
  const featuredMeta = featured
    ? (featuredSlug && categoryMetaBySlug[featuredSlug as keyof typeof categoryMetaBySlug]) || defaultCategoryMeta
    : null;
  const featuredName = featured ? getString(featured, "name", "title") ?? "Category" : null;

  return (
    <div className="space-y-8">
      <section className="market-hero grid gap-6 p-5 sm:p-6 lg:grid-cols-[1.1fr_0.9fr] lg:items-center lg:gap-8 lg:p-8">
        <div className="relative z-10 max-w-xl space-y-5">
          <div className="space-y-3">
            <h1 className="text-3xl font-semibold tracking-tight leading-tight sm:text-4xl">Shop by category</h1>
            <p className="text-base leading-8 text-muted-foreground sm:leading-7">
              Pick a lane and start shopping in seconds.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-3">
            <Link
              to="/products"
              className="inline-flex items-center gap-2 rounded-md bg-primary px-4 py-2 text-sm font-semibold text-primary-foreground transition hover:bg-primary/90"
            >
              Shop all
              <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M5 12h14" />
                <path d="M13 6l6 6-6 6" />
              </svg>
            </Link>
            <div className="inline-flex items-center gap-2 rounded-md border border-border bg-white px-4 py-2 text-sm text-muted-foreground">
              New arrivals daily
            </div>
          </div>
        </div>
        <div className="hero-media min-h-[260px] lg:min-h-[320px]">
          <SafeImage
            src="https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=900&q=80"
            alt="Trending fashion"
            fallbackKey="categories-hero-fashion"
            className="h-full w-full object-cover"
          />
          <div className="hero-media__overlay" />
          <div className="hero-media__badge">Trending now</div>
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
        <div className="grid gap-4 lg:grid-cols-3">
          {featured && featuredMeta && featuredName ? (
            <Link to={`/categories/${featuredId ?? 0}`} className="block lg:col-span-2 lg:row-span-2">
              <Card className="category-card pressable group relative h-full overflow-hidden border transition">
                <div className="relative h-56 overflow-hidden sm:h-64 lg:h-full">
                  <SafeImage
                    src={featuredMeta.imageUrl}
                    alt={featuredName}
                    fallbackKey={`category-featured-${featuredSlug ?? featuredId ?? "default"}`}
                    className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                  />
                  <div className="absolute inset-0 bg-gradient-to-t from-slate-900/55 via-slate-900/15 to-transparent" />
                  <span className={cn(
                    "absolute left-5 top-5 inline-flex items-center rounded-full border px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em]",
                    featuredMeta.accentClassName
                  )}>
                    {featuredMeta.badge ?? "Hot pick"}
                  </span>
                  <div className="absolute bottom-5 left-5 right-5 space-y-3 text-white">
                    <div className="text-xs font-semibold uppercase tracking-[0.22em] text-white/80">Featured</div>
                    <h2 className="text-3xl font-semibold">{featuredName}</h2>
                    <p className="max-w-md text-sm text-white/85">{featuredMeta.description}</p>
                    <div className="inline-flex items-center gap-2 rounded-md bg-white/10 px-4 py-2 text-sm font-semibold text-white backdrop-blur-sm transition hover:bg-white/20">
                      Shop category
                      <svg viewBox="0 0 24 24" className="h-4 w-4" fill="none" stroke="currentColor" strokeWidth="2">
                        <path d="M5 12h14" />
                        <path d="M13 6l6 6-6 6" />
                      </svg>
                    </div>
                  </div>
                </div>
              </Card>
            </Link>
          ) : null}

          {categories.filter((c) => getNumber(c, "id") !== featuredId).map((c) => {
            const id = getNumber(c, "id") ?? 0;
            const name = getString(c, "name", "title") ?? "Category";
            const slug = getString(c, "slug");
            const href = `/categories/${id}`;
            const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
            return (
              <Link key={id} to={href} className="block cursor-pointer">
                <Card className="category-card pressable group relative overflow-hidden border transition">
                  <div className="relative h-36 overflow-hidden">
                    <SafeImage
                      src={meta.imageUrl}
                      alt={name}
                      fallbackKey={`category-${slug ?? id}`}
                      className="h-full w-full object-cover transition-transform duration-300 group-hover:scale-105"
                    />
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-900/45 via-slate-900/5 to-transparent" />
                  </div>
                  <CardHeader className="space-y-3 pb-2">
                    <div className="flex items-start justify-between gap-3">
                      <CategoryIcon
                        name={meta.icon}
                        className="border-white/60 bg-white shadow-sm"
                      />
                      {meta.badge ? (
                        <span className={cn(
                          "inline-flex items-center rounded-full border px-2.5 py-1 text-[11px] font-semibold uppercase tracking-[0.18em]",
                          meta.accentClassName
                        )}>
                          {meta.badge}
                        </span>
                      ) : null}
                    </div>
                    <div className="space-y-2">
                      <CardTitle className="text-lg">{name}</CardTitle>
                      <div className="text-sm text-muted-foreground">
                        {meta.description}
                      </div>
                    </div>
                  </CardHeader>
                  <CardContent className="pt-2">
                    <div className="inline-flex items-center gap-2 rounded-md border border-primary/20 px-3 py-2 text-sm font-semibold text-primary transition group-hover:-translate-y-0.5 group-hover:bg-primary group-hover:text-primary-foreground">
                      Shop category
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
