import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import ProductCard from "@/components/ProductCard";
import SafeImage from "@/components/SafeImage";
import { apiJson } from "@/lib/http";
import { apiGet, buildQuery } from "@/lib/apiClient";
import { getNumber, getString } from "@/lib/safe";
import { getAppBaseUrl, isAbsoluteUrl } from "@/lib/env";
import { Link, useNavigate } from "react-router-dom";
import { useEffect, useMemo, useRef, useState } from "react";
import { categoryMetaBySlug, defaultCategoryMeta } from "@/lib/categoryMeta";
import CategoryIcon from "@/components/CategoryIcon";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/app/AuthProvider";
import { getStoredGuestId } from "@/lib/cartApi";
import { listMyRecentViews, type RecentViewResponse } from "@/lib/recentViewApi";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";

type Banner = unknown;
type Product = unknown;
type Category = unknown;

type PreparedBanner = {
  key: string;
  title: string;
  imageUrl: string;
  isInternal: boolean;
  targetPath: string;
  targetUrl: string;
};

const BANNER_COOLDOWN_MS = 30 * 60 * 1000;
const HERO_AUTOPLAY_MS = 4200;
const DISMISSED_BANNERS_KEY = "home-dismissed-banners";
const BANNER_LAST_SHOWN_KEY = "home-banner-last-shown-at";
const BANNER_HIDE_UNTIL_KEY = "home-banner-hide-until";
const HERO_CHIP_SETS = [
  ["Freeship 0D", "Extra 10% off"],
  ["Flash sale", "Payday picks"],
  ["Voucher x2", "Fast delivery"],
] as const;

function formatAsClock(totalSeconds: number) {
  const h = Math.floor(totalSeconds / 3600)
    .toString()
    .padStart(2, "0");
  const m = Math.floor((totalSeconds % 3600) / 60)
    .toString()
    .padStart(2, "0");
  const s = Math.floor(totalSeconds % 60)
    .toString()
    .padStart(2, "0");
  return `${h}:${m}:${s}`;
}

function getFeaturedTitle(rawTitle: string, targetPath: string, categories: Category[], fallbackIndex: number) {
  const title = rawTitle.trim();
  if (title && title.length >= 6 && !/^banner\s*#?\d+$/i.test(title)) {
    return title;
  }

  const categoryMatch = targetPath.match(/^\/categories\/(\d+)/);
  if (categoryMatch) {
    const categoryId = Number(categoryMatch[1]);
    const category = categories.find((c) => (getNumber(c, "id") ?? 0) === categoryId);
    const categoryName = getString(category, "name");
    if (categoryName) {
      return `Top picks: ${categoryName}`;
    }
  }

  if (targetPath.startsWith("/products")) {
    return "Trending picks for you";
  }

  return `Featured offer #${fallbackIndex + 1}`;
}

const topListOptions = [
  { key: "top-rating", label: "Top rated" },
  { key: "most-favorite", label: "Most saved" },
  { key: "most-viewed", label: "Most viewed" },
  { key: "best-selling", label: "Best sellers" },
] as const;

export default function HomePage() {
  const navigate = useNavigate();
  const auth = useAuth();
  const [home, setHome] = useState<unknown>(null);
  const [banners, setBanners] = useState<Banner[]>([]);
  const [homeCategories, setHomeCategories] = useState<Category[]>([]);
  const [topKey, setTopKey] =
    useState<(typeof topListOptions)[number]["key"]>("top-rating");
  const [topProducts, setTopProducts] = useState<Product[]>([]);
  const [topPage, setTopPage] = useState(0);
  const topPageSize = 6;
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [dismissedBannerKeys, setDismissedBannerKeys] = useState<string[]>([]);
  const [lastBannerShownAt, setLastBannerShownAt] = useState(0);
  const [hideFloatingUntil, setHideFloatingUntil] = useState(0);
  const [heroIndex, setHeroIndex] = useState(0);
  const [heroNowMs, setHeroNowMs] = useState(() => Date.now());
  const heroDragStartX = useRef(0);
  const heroDragDistance = useRef(0);
  const heroIsDragging = useRef(false);
  const heroBlockNextClick = useRef(false);

  const [recentViews, setRecentViews] = useState<RecentViewResponse[]>([]);

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
        setError(getErrorMessage(e, "Failed to load homepage."));
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
    if (!auth.isAuthenticated) {
      setRecentViews([]);
      return;
    }
    listMyRecentViews({ page: 0, size: 12 })
      .then((page) => setRecentViews(page.content ?? []))
      .catch(() => setRecentViews([]));
  }, [auth.isAuthenticated]);

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
        setError(getErrorMessage(e, "Failed to load products."));
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
  const featuredTop = topPageItems[0];
  const restTop = topPageItems.slice(1);
  const dealProducts = topProducts.slice(0, 12);
  const dealGroups = [
    dealProducts.slice(0, 6),
    dealProducts.slice(6, 12),
  ];
  const dealTitles = [
    "Top picks in Electronics",
    "Fresh styles for you",
  ];

  const preparedBanners = useMemo<PreparedBanner[]>(() => {
    return (banners ?? []).map((b, index) => {
      const rawTitle = getString(b, "title") ?? "";
      const imageUrl = getString(b, "imageUrl", "image", "url") ?? "";
      const targetPath = getString(b, "targetUrl", "targetPath", "url") ?? "/products";
      const title = getFeaturedTitle(rawTitle, targetPath, homeCategories, index);
      const isInternal = !isAbsoluteUrl(targetPath) && targetPath.startsWith("/");
      const targetUrl = isAbsoluteUrl(targetPath)
        ? targetPath
        : `${getAppBaseUrl()}${targetPath.startsWith("/") ? "" : "/"}${targetPath}`;
      const key = String(getNumber(b, "id") ?? `${title}-${targetPath}-${index}`);
      return { key, title, imageUrl, targetPath, isInternal, targetUrl };
    });
  }, [banners, homeCategories]);

  const heroBanners = useMemo<PreparedBanner[]>(() => {
    const picks = (banners ?? []).map((b, index) => {
      const rawTitle = getString(b, "title") ?? "";
      const imageUrl = getString(b, "imageUrl", "image", "url") ?? "";
      const targetPath = getString(b, "targetUrl", "targetPath", "url") ?? "/products";
      const title = getFeaturedTitle(rawTitle, targetPath, homeCategories, index);
      const isInternal = !isAbsoluteUrl(targetPath) && targetPath.startsWith("/");
      const targetUrl = isAbsoluteUrl(targetPath)
        ? targetPath
        : `${getAppBaseUrl()}${targetPath.startsWith("/") ? "" : "/"}${targetPath}`;
      const key = String(getNumber(b, "id") ?? `hero-${title}-${targetPath}-${index}`);
      return { key, title, imageUrl, targetPath, isInternal, targetUrl };
    });

    if (picks.length > 0) {
      return picks;
    }

    return [
      {
        key: "hero-fallback",
        title: "Big deals, fresh arrivals, every day",
        imageUrl: "",
        isInternal: true,
        targetPath: "/products",
        targetUrl: `${getAppBaseUrl()}/products`,
      },
    ];
  }, [banners, homeCategories]);

  const heroSideBanners = useMemo(() => {
    if (heroBanners.length <= 1) {
      return [] as PreparedBanner[];
    }
    const sideCount = Math.min(2, Math.max(0, heroBanners.length - 1));
    return Array.from({ length: sideCount }, (_, idx) => {
      const nextIdx = (heroIndex + idx + 1) % heroBanners.length;
      return heroBanners[nextIdx];
    });
  }, [heroBanners, heroIndex]);
  const featuredBanners = useMemo(() => preparedBanners.slice(0, 4), [preparedBanners]);
  const floatingBanners = useMemo(
    () => featuredBanners.filter((b) => !dismissedBannerKeys.includes(b.key)).slice(0, 2),
    [featuredBanners, dismissedBannerKeys]
  );
  const mobileFloatingBanner = useMemo(
    () => floatingBanners[0] ?? null,
    [floatingBanners]
  );
  const canShowFloating = useMemo(
    () => Date.now() >= hideFloatingUntil && Date.now() - lastBannerShownAt >= BANNER_COOLDOWN_MS,
    [hideFloatingUntil, lastBannerShownAt]
  );
  const heroSecondsUntilMidnight = useMemo(() => {
    const now = new Date(heroNowMs);
    const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1).getTime();
    return Math.max(0, Math.floor((endOfDay - heroNowMs) / 1000));
  }, [heroNowMs]);

  useEffect(() => {
    if (!heroBanners.length) {
      setHeroIndex(0);
      return;
    }
    setHeroIndex((prev) => (prev >= heroBanners.length ? 0 : prev));
  }, [heroBanners]);

  useEffect(() => {
    if (heroBanners.length <= 1) return;
    const timer = window.setInterval(() => {
      if (heroIsDragging.current) return;
      setHeroIndex((prev) => (prev + 1) % heroBanners.length);
    }, HERO_AUTOPLAY_MS);
    return () => window.clearInterval(timer);
  }, [heroBanners.length]);

  useEffect(() => {
    const timer = window.setInterval(() => setHeroNowMs(Date.now()), 1000);
    return () => window.clearInterval(timer);
  }, []);

  function goToNextHero() {
    if (heroBanners.length <= 1) return;
    setHeroIndex((prev) => (prev + 1) % heroBanners.length);
  }

  function goToPrevHero() {
    if (heroBanners.length <= 1) return;
    setHeroIndex((prev) => (prev - 1 + heroBanners.length) % heroBanners.length);
  }

  function onHeroSlideClick(event: React.MouseEvent<HTMLElement>, hero: PreparedBanner) {
    if (heroBlockNextClick.current) {
      heroBlockNextClick.current = false;
      return;
    }

    const target = event.target as HTMLElement | null;
    if (target?.closest("a, button, input, select, textarea, [role='button']")) {
      return;
    }

    trackBannerClick(hero.key, "hero", hero.targetPath);
    if (hero.isInternal) {
      navigate(hero.targetPath);
    } else {
      window.location.href = hero.targetUrl;
    }
  }

  function onHeroPointerDown(e: React.PointerEvent<HTMLDivElement>) {
    if (heroBanners.length <= 1) return;

    const target = e.target as HTMLElement | null;
    if (target?.closest("a, button, input, select, textarea, [role='button']")) {
      return;
    }

    heroIsDragging.current = true;
    heroDragStartX.current = e.clientX;
    heroDragDistance.current = 0;
  }

  function onHeroPointerMove(e: React.PointerEvent<HTMLDivElement>) {
    if (!heroIsDragging.current) return;
    heroDragDistance.current = e.clientX - heroDragStartX.current;
  }

  function onHeroPointerUp() {
    if (!heroIsDragging.current) return;
    heroIsDragging.current = false;
    if (heroDragDistance.current <= -60) {
      heroBlockNextClick.current = true;
      goToNextHero();
    } else if (heroDragDistance.current >= 60) {
      heroBlockNextClick.current = true;
      goToPrevHero();
    }
    heroDragDistance.current = 0;
  }

  function trackBannerClick(bannerKey: string, placement: string, targetPath: string) {
    const guestId = getStoredGuestId();
    void apiJson<void>("/api/public/home/banner-click", {
      method: "POST",
      auth: false,
      body: {
        bannerKey,
        placement,
        targetPath,
        guestId: guestId ?? undefined,
      },
    }).catch(() => undefined);
  }

  function dismissFloatingForToday() {
    const now = new Date();
    const endOfDay = new Date(now.getFullYear(), now.getMonth(), now.getDate() + 1).getTime();
    setHideFloatingUntil(endOfDay);
    window.localStorage.setItem(BANNER_HIDE_UNTIL_KEY, String(endOfDay));
  }

  useEffect(() => {
    const raw = window.sessionStorage.getItem(DISMISSED_BANNERS_KEY);
    if (!raw) return;
    try {
      const parsed = JSON.parse(raw);
      if (Array.isArray(parsed)) {
        setDismissedBannerKeys(parsed.filter((v) => typeof v === "string"));
      }
    } catch {
      // ignore malformed storage
    }
  }, []);

  useEffect(() => {
    const raw = window.sessionStorage.getItem(BANNER_LAST_SHOWN_KEY);
    if (!raw) return;
    const parsed = Number(raw);
    if (Number.isFinite(parsed) && parsed > 0) {
      setLastBannerShownAt(parsed);
    }
  }, []);

  useEffect(() => {
    const raw = window.localStorage.getItem(BANNER_HIDE_UNTIL_KEY);
    if (!raw) return;
    const parsed = Number(raw);
    if (Number.isFinite(parsed) && parsed > Date.now()) {
      setHideFloatingUntil(parsed);
    }
  }, []);

  useEffect(() => {
    window.sessionStorage.setItem(DISMISSED_BANNERS_KEY, JSON.stringify(dismissedBannerKeys));
  }, [dismissedBannerKeys]);

  useEffect(() => {
    if (!canShowFloating || !floatingBanners.length) return;
    const now = Date.now();
    setLastBannerShownAt(now);
    window.sessionStorage.setItem(BANNER_LAST_SHOWN_KEY, String(now));
  }, [canShowFloating, floatingBanners.length]);

  return (
    <div className="space-y-8">
      <section className="market-hero market-hero--fullbleed">
        <div className="market-hero-frame">
          <div className="market-hero-main">
            <div
              className="market-hero-slider"
              onPointerDown={onHeroPointerDown}
              onPointerMove={onHeroPointerMove}
              onPointerUp={onHeroPointerUp}
              onPointerCancel={onHeroPointerUp}
            >
              <div
                className="market-hero-slider__track"
                style={{ transform: `translateX(-${heroIndex * 100}%)` }}
              >
                {heroBanners.map((hero, index) => (
                  <article
                    key={hero.key}
                    className="market-hero__slide cursor-pointer"
                    onClick={(event) => onHeroSlideClick(event, hero)}
                  >
                    {hero.imageUrl ? (
                      <SafeImage
                        src={hero.imageUrl}
                        alt={hero.title}
                        fallbackKey={`home-hero-${hero.key}-${index}`}
                        className="h-full w-full object-cover"
                      />
                    ) : (
                      <div className="h-full w-full bg-muted" />
                    )}

                    <div className="market-hero__content">
                      <div className="mx-auto w-full max-w-6xl px-4">
                        <div className="max-w-xl space-y-3 text-white">
                          <div className="text-xs font-semibold uppercase tracking-[0.22em] text-white/80">Today</div>
                          <div className="market-hero__chips">
                            {HERO_CHIP_SETS[index % HERO_CHIP_SETS.length].map((chip) => (
                              <span key={`${hero.key}-${chip}`} className="market-hero__chip">{chip}</span>
                            ))}
                            <span className="market-hero__timer">Ends in {formatAsClock(heroSecondsUntilMidnight)}</span>
                          </div>
                          <h1 className="text-3xl font-semibold leading-tight sm:text-4xl">{hero.title}</h1>
                          <p className="text-sm text-white/85">Big deals, fast shipping, trusted sellers.</p>
                          <div className="flex flex-wrap gap-2">
                            {hero.isInternal ? (
                              <Button asChild className="h-10 rounded-md bg-primary text-primary-foreground">
                                <Link to={hero.targetPath} onClick={() => trackBannerClick(hero.key, "hero", hero.targetPath)}>
                                  Grab the deal
                                </Link>
                              </Button>
                            ) : (
                              <a
                                href={hero.targetUrl}
                                className="inline-flex h-10 items-center rounded-md bg-primary px-4 text-sm font-semibold text-primary-foreground"
                                onClick={() => trackBannerClick(hero.key, "hero", hero.targetPath)}
                              >
                                Grab the deal
                              </a>
                            )}
                            <Button asChild variant="outline" className="h-10 rounded-md bg-white/90">
                              <Link to="/categories">Browse categories</Link>
                            </Button>
                          </div>
                        </div>
                      </div>
                    </div>
                  </article>
                ))}
              </div>

              {heroBanners.length > 1 ? (
                <>
                  <div className="market-hero-slider__progress">
                    <span
                      key={`hero-progress-${heroIndex}`}
                      className="market-hero-slider__progress-bar"
                      style={{ animationDuration: `${HERO_AUTOPLAY_MS}ms` }}
                    />
                  </div>
                  <button
                    type="button"
                    aria-label="Previous banner"
                    className="market-hero-slider__arrow market-hero-slider__arrow--left"
                    onClick={goToPrevHero}
                  >
                    <span aria-hidden>‹</span>
                  </button>
                  <button
                    type="button"
                    aria-label="Next banner"
                    className="market-hero-slider__arrow market-hero-slider__arrow--right"
                    onClick={goToNextHero}
                  >
                    <span aria-hidden>›</span>
                  </button>
                  <div className="market-hero-slider__dots">
                    {heroBanners.map((hero, idx) => (
                      <button
                        key={`hero-dot-${hero.key}`}
                        type="button"
                        aria-label={`Open banner ${idx + 1}`}
                        className={`market-hero-slider__dot ${heroIndex === idx ? "is-active" : ""}`}
                        onClick={() => setHeroIndex(idx)}
                      />
                    ))}
                  </div>
                </>
              ) : null}
            </div>
          </div>

          <aside className="market-hero-side" aria-label="Promotions">
            {heroSideBanners.map((banner, idx) => (
              <article key={`hero-side-${banner.key}`} className="market-hero-side__card">
                {banner.isInternal ? (
                  <Link
                    to={banner.targetPath}
                    className="block h-full cursor-pointer"
                    onClick={() => trackBannerClick(banner.key, "hero-side", banner.targetPath)}
                  >
                    <div className="market-hero-side__image-wrap">
                      <SafeImage
                        src={banner.imageUrl}
                        alt={banner.title}
                        fallbackKey={`hero-side-${banner.key}-${idx}`}
                        className="h-full w-full object-cover"
                      />
                    </div>
                    <div className="market-hero-side__label">{banner.title}</div>
                  </Link>
                ) : (
                  <a
                    href={banner.targetUrl}
                    className="block h-full cursor-pointer"
                    onClick={() => trackBannerClick(banner.key, "hero-side", banner.targetPath)}
                  >
                    <div className="market-hero-side__image-wrap">
                      <SafeImage
                        src={banner.imageUrl}
                        alt={banner.title}
                        fallbackKey={`hero-side-${banner.key}-${idx}`}
                        className="h-full w-full object-cover"
                      />
                    </div>
                    <div className="market-hero-side__label">{banner.title}</div>
                  </a>
                )}
              </article>
            ))}
          </aside>
        </div>
      </section>

      {dealProducts.length ? (
        <section className="grid gap-4 lg:grid-cols-2">
          {dealGroups.map((group, idx) => {
            if (!group.length) return null;
            return (
              <div key={`deal-${idx}`} className="market-card rounded-md border bg-card p-4 sm:p-5">
                <div className="text-base font-semibold">{dealTitles[idx] ?? "Top picks"}</div>
                <div className="mt-3 grid grid-cols-2 gap-3 lg:grid-cols-3">
                  {group.map((p, subIdx) => {
                    const id = getNumber(p, "id") ?? subIdx + 1;
                    const name = getString(p, "name", "title") ?? "Product";
                    const imageUrl =
                      getString(p, "primaryImageUrl", "imageUrl", "thumbnailUrl") ??
                      getString(p, "url") ??
                      "";
                    return (
                      <Link key={`deal-${idx}-${id}`} to={`/products/${id}`} className="group">
                        <div className="aspect-[4/3] overflow-hidden rounded-md bg-muted/40">
                          <SafeImage
                            src={imageUrl}
                            alt={name}
                            fallbackKey={`deal-${idx}-${id}`}
                            className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
                          />
                        </div>
                        <div className="mt-1 line-clamp-1 text-sm text-muted-foreground">{name}</div>
                      </Link>
                    );
                  })}
                </div>
                <Link to="/products" className="mt-3 inline-flex text-sm font-semibold text-primary">
                  Explore deals
                </Link>
              </div>
            );
          })}
        </section>
      ) : null}

      {auth.isAuthenticated && recentViews.length ? (
        <section className="space-y-3">
          <div className="flex items-end justify-between gap-3">
            <div>
              <div className="text-xl font-semibold tracking-tight">Recently viewed</div>
            </div>
            <Button asChild variant="outline" className="h-9 rounded-md">
              <Link to="/products">View all</Link>
            </Button>
          </div>
          <div className="flex gap-3 overflow-auto pb-2 [-ms-overflow-style:none] [scrollbar-width:none] [&::-webkit-scrollbar]:hidden snap-x snap-mandatory">
            {recentViews.slice(0, 12).map((rv) => (
              <Link key={String(rv.id)} to={`/products/${rv.productId ?? ""}`} className="group w-60 shrink-0 snap-start">
                <div className="pressable overflow-hidden rounded-md border bg-background shadow-sm transition hover:shadow-md">
                  <div className="aspect-[4/3] overflow-hidden bg-muted">
                    <SafeImage
                      src={rv.url || ""}
                      alt={rv.productName || "Product"}
                      fallbackKey={String(rv.id ?? rv.productId ?? "recent")}
                      className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.03]"
                    />
                  </div>
                  <div className="p-3">
                    <div className="truncate text-sm font-semibold">{rv.productName || "Product"}</div>
                  </div>
                </div>
              </Link>
            ))}
          </div>
        </section>
      ) : null}

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
                  See all
                </Link>
              </div>
              <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-4">
                {homeCategories.slice(0, 8).map((c, idx) => {
                  const id = getNumber(c, "id") ?? idx + 1;
                  const name = getString(c, "name") ?? "Category";
                  const slug = getString(c, "slug");
                  const meta = (slug && categoryMetaBySlug[slug]) || defaultCategoryMeta;
                  return (
                    <Link
                      key={String(id)}
                      to={`/categories/${id}`}
                      className="pressable group flex items-center gap-3 rounded-md border bg-card p-3 transition hover:shadow-md"
                    >
                      <CategoryIcon
                        name={meta.icon}
                        className="transition duration-300 "
                      />
                      <div className="min-w-0">
                        <div className="truncate text-sm font-semibold">{name}</div>
                      </div>
                    </Link>
                  );
                })}
              </div>
            </section>
          ) : null}

          <section className="space-y-3">
            <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
              <h2 className="text-lg font-semibold tracking-tight">Best sellers this week</h2>
              <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                <div className="flex items-center gap-2">
                  <select
                    aria-label="Sort top products"
                    title="Sort top products"
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
              </div>
            </div>

            {topProducts.length ? (
              <div className="space-y-4">
                <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
                  {featuredTop ? (() => {
                    const id = getNumber(featuredTop, "id") ?? 0;
                    const name = getString(featuredTop, "name", "title") ?? "Product";
                    const price = getNumber(featuredTop, "salePrice", "price");
                    const currency = getString(featuredTop, "currency") ?? "VND";
                    const desc = getString(featuredTop, "description") ?? "Limited-time deal. Grab it before it is gone.";
                    const imageUrl =
                      getString(featuredTop, "primaryImageUrl", "imageUrl", "thumbnailUrl") ??
                      getString(featuredTop, "url") ??
                      "";
                    return (
                      <Link key={`featured-${id}`} to={`/products/${id}`} className="pressable market-card overflow-hidden lg:col-span-2">
                        <div className="grid gap-3 md:grid-cols-[1.2fr_1fr]">
                          <div className="aspect-[4/3] overflow-hidden bg-muted/40">
                            <SafeImage
                              src={imageUrl}
                              alt={name}
                              fallbackKey={`featured-${id}`}
                              className="h-full w-full object-cover transition duration-300 hover:scale-[1.03]"
                            />
                          </div>
                          <div className="p-4 space-y-2">
                            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Big deal</div>
                            <div className="text-lg font-semibold">{name}</div>
                            <div className="text-sm text-muted-foreground line-clamp-3">{desc}</div>
                            <div className="text-lg font-bold text-primary">{formatCurrency(price, currency)}</div>
                            <div className="inline-flex items-center rounded-md bg-primary px-3 py-2 text-sm font-semibold text-primary-foreground">
                              Grab the deal
                            </div>
                          </div>
                        </div>
                      </Link>
                    );
                  })() : null}
                  {restTop.map((p, index) => {
                    const id = getNumber(p, "id") ?? index + 1;
                    return <ProductCard key={String(id)} product={p} href={`/products/${id}`} />;
                  })}
                </div>

                <div className="flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                  <div />
                  <div className="flex items-center justify-end gap-2">
                    <Button
                      type="button"
                      variant="outline"
                      disabled={topPageClamped <= 0}
                      onClick={() => setTopPage((p) => Math.max(0, p - 1))}
                    >
                      Prev
                    </Button>
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

      {!isLoading && canShowFloating && floatingBanners.length ? (
        <div className="pointer-events-none fixed inset-0 z-40 hidden xl:block">
          {floatingBanners.map((b, idx) => {
            const placement = idx % 2 === 0 ? "left-4" : "right-4";
            const top = idx % 2 === 0 ? "top-24" : "top-56";
            return (
              <div key={`floating-${b.key}`} className={`pointer-events-auto floating-banner-enter absolute ${placement} ${top} w-[220px]`}>
                <div className="overflow-hidden rounded-md border bg-white/95 shadow-lg backdrop-blur-sm">
                  <button
                    type="button"
                    aria-label="Dismiss banner"
                    className="absolute right-2 top-2 z-10 grid h-6 w-6 place-items-center rounded-full bg-black/55 text-xs font-semibold text-white"
                    onClick={() => setDismissedBannerKeys((prev) => (prev.includes(b.key) ? prev : [...prev, b.key]))}
                  >
                    x
                  </button>
                  {b.isInternal ? (
                    <Link
                      to={b.targetPath}
                      className="block"
                      onClick={() => trackBannerClick(b.key, "floating", b.targetPath)}
                    >
                      <div className="aspect-[3/4] bg-muted/40">
                        <SafeImage
                          src={b.imageUrl}
                          alt={b.title}
                          fallbackKey={`floating-${b.key}`}
                          className="h-full w-full object-cover"
                        />
                      </div>
                      <div className="line-clamp-2 p-2 text-xs font-semibold">{b.title}</div>
                    </Link>
                  ) : (
                    <a
                      href={b.targetUrl}
                      className="block"
                      onClick={() => trackBannerClick(b.key, "floating", b.targetPath)}
                    >
                      <div className="aspect-[3/4] bg-muted/40">
                        <SafeImage
                          src={b.imageUrl}
                          alt={b.title}
                          fallbackKey={`floating-${b.key}`}
                          className="h-full w-full object-cover"
                        />
                      </div>
                      <div className="line-clamp-2 p-2 text-xs font-semibold">{b.title}</div>
                    </a>
                  )}
                  <button
                    type="button"
                    className="w-full border-t bg-muted/20 px-2 py-1.5 text-[11px] font-semibold text-muted-foreground hover:bg-muted/35"
                    onClick={dismissFloatingForToday}
                  >
                    Hide today
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      ) : null}

      {!isLoading && canShowFloating && mobileFloatingBanner ? (
        <div className="pointer-events-none fixed inset-x-3 bottom-3 z-40 floating-banner-enter xl:hidden">
          <div className="pointer-events-auto relative overflow-hidden rounded-md border bg-white/95 shadow-lg backdrop-blur-sm">
            <button
              type="button"
              aria-label="Dismiss banner"
              className="absolute right-2 top-2 z-10 grid h-6 w-6 place-items-center rounded-full bg-black/55 text-xs font-semibold text-white"
              onClick={() => setDismissedBannerKeys((prev) => (prev.includes(mobileFloatingBanner.key) ? prev : [...prev, mobileFloatingBanner.key]))}
            >
              x
            </button>
            {mobileFloatingBanner.isInternal ? (
              <Link
                to={mobileFloatingBanner.targetPath}
                className="flex items-center gap-3 p-2 pr-8"
                onClick={() => trackBannerClick(mobileFloatingBanner.key, "mobile-floating", mobileFloatingBanner.targetPath)}
              >
                <div className="h-16 w-24 shrink-0 overflow-hidden rounded bg-muted/40">
                  <SafeImage
                    src={mobileFloatingBanner.imageUrl}
                    alt={mobileFloatingBanner.title}
                    fallbackKey={`mobile-floating-${mobileFloatingBanner.key}`}
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="line-clamp-2 text-sm font-semibold">{mobileFloatingBanner.title}</div>
              </Link>
            ) : (
              <a
                href={mobileFloatingBanner.targetUrl}
                className="flex items-center gap-3 p-2 pr-8"
                onClick={() => trackBannerClick(mobileFloatingBanner.key, "mobile-floating", mobileFloatingBanner.targetPath)}
              >
                <div className="h-16 w-24 shrink-0 overflow-hidden rounded bg-muted/40">
                  <SafeImage
                    src={mobileFloatingBanner.imageUrl}
                    alt={mobileFloatingBanner.title}
                    fallbackKey={`mobile-floating-${mobileFloatingBanner.key}`}
                    className="h-full w-full object-cover"
                  />
                </div>
                <div className="line-clamp-2 text-sm font-semibold">{mobileFloatingBanner.title}</div>
              </a>
            )}
            <button
              type="button"
              className="w-full border-t bg-muted/20 px-2 py-1.5 text-[11px] font-semibold text-muted-foreground"
              onClick={dismissFloatingForToday}
            >
              Hide today
            </button>
          </div>
        </div>
      ) : null}
    </div>
  );
}

