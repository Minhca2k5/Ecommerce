import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { filterMyVouchersByMinOrderAmount, getMyVouchersByCode, type VoucherResponse } from "@/lib/voucherApi";

function toNumber(value: number | string | undefined) {
  if (value === undefined || value === null) return 0;
  const n = typeof value === "string" ? Number(value) : value;
  return Number.isFinite(n) ? n : 0;
}

function money(value: number | string | undefined, currency = "VND") {
  const n = toNumber(value);
  return formatCurrency(n, currency);
}

function badgeForUses(remaining?: number) {
  const n = typeof remaining === "number" ? remaining : 0;
  const cls = n > 0 ? "bg-emerald-500/10 text-emerald-700 ring-emerald-500/20" : "bg-rose-500/10 text-rose-700 ring-rose-500/20";
  return (
    <span className={["rounded-full px-2 py-1 text-xs ring-1", cls].join(" ")}>
      {n > 0 ? `${n} left` : "Not available"}
    </span>
  );
}

export default function MyVouchersPage() {
  const [items, setItems] = useState<VoucherResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(12);
  const [totalPages, setTotalPages] = useState(1);

  const [searchCode, setSearchCode] = useState("");
  const [searchStatus, setSearchStatus] = useState<"idle" | "loading" | "done">("idle");
  const [searchResults, setSearchResults] = useState<VoucherResponse[]>([]);
  const [searchError, setSearchError] = useState<string | null>(null);

  const hasSearch = Boolean(searchCode.trim());

  async function loadAvailable() {
    setIsLoading(true);
    setError(null);
    try {
      // Use a very large minOrderAmount to fetch "all" available vouchers for the current user.
      const res = await filterMyVouchersByMinOrderAmount(999999999, { page, size });
      setItems(res.content ?? []);
      setTotalPages(Number(res.totalPages ?? 1) || 1);
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load vouchers."));
    } finally {
      setIsLoading(false);
    }
  }

  async function runSearch() {
    const code = searchCode.trim();
    if (!code) return;
    setSearchStatus("loading");
    setSearchError(null);
    try {
      const res = await getMyVouchersByCode(code);
      setSearchResults(res ?? []);
    } catch (e) {
      setSearchError(getErrorMessage(e, "Search failed."));
    } finally {
      setSearchStatus("done");
    }
  }

  useEffect(() => {
    void loadAvailable();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page, size]);

  const headline = useMemo(() => (hasSearch ? `Results for "${searchCode.trim()}"` : "Available discounts"), [hasSearch, searchCode]);

  return (
    <div className="space-y-8">
      <section className="page-hero">
        <div className="hero-orb hero-orb--a" />
        <div className="hero-orb hero-orb--b" />
        <div className="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="max-w-2xl space-y-2">
            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Vouchers</div>
            <div className="text-3xl font-semibold tracking-tight">Your vouchers</div>
            <p className="text-sm text-muted-foreground">
              Search by code or browse your discounts.
            </p>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="h-10 rounded-md bg-white">
              <Link to="/me/voucher-uses">Usage</Link>
            </Button>
          </div>
        </div>
      </section>

      <Card className="panel-card">
        <CardHeader>
          <CardTitle className="text-base">Search by code</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-[1fr_auto] sm:items-end">
          <div className="space-y-2">
            <div className="text-xs font-medium text-muted-foreground">Voucher code</div>
            <Input
              className="rounded-md bg-white"
              value={searchCode}
              onChange={(e) => {
                setSearchCode(e.target.value);
                setSearchResults([]);
                setSearchError(null);
                setSearchStatus("idle");
              }}
              placeholder="e.g. SAVE10"
            />
          </div>
          <Button
            className="h-10 rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
            onClick={runSearch}
            disabled={!searchCode.trim() || searchStatus === "loading"}
          >
            {searchStatus === "loading" ? "Searching..." : "Search"}
          </Button>
        </CardContent>
      </Card>

      {searchError ? (
        <EmptyState title="Search failed" description={searchError} />
      ) : null}

      {isLoading ? (
        <div className="space-y-4">
          <LoadingCard />
          <LoadingCard />
        </div>
      ) : error ? (
        <EmptyState
          title="Could not load vouchers"
          description={error}
          action={
            <Button onClick={loadAvailable} className="h-10 rounded-md bg-primary text-primary-foreground">
              Try again
            </Button>
          }
        />
      ) : (
        <>
          <div className="flex items-center justify-between gap-3">
            <div className="text-sm font-medium">{headline}</div>
            {!hasSearch ? (
              <div className="flex items-center gap-2">
                <select title="Select option" value={String(size)} onChange={(e) => setSize(Number(e.target.value))} className="h-9 rounded-md border bg-white px-3 text-sm">
                  {[12, 24, 36].map((n) => (
                    <option key={n} value={String(n)}>
                      {n}/page
                    </option>
                  ))}
                </select>
                <Button variant="outline" className="h-9 rounded-md" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                  Prev
                </Button>
                <Button
                  variant="outline"
                  className="h-9 rounded-md"
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                >
                  Next
                </Button>
              </div>
            ) : null}
          </div>

          {(hasSearch ? searchResults : items).length === 0 ? (
            <EmptyState title="No vouchers found" description={hasSearch ? "No voucher matches that code." : "No vouchers available right now."} />
          ) : (
            <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
              {(hasSearch ? searchResults : items).map((v) => {
                const id = Number(v.id ?? 0);
                const remaining = typeof v.activeUsesForUser === "number" ? v.activeUsesForUser : undefined;
                return (
                  <Card key={String(id || v.code)} className="pressable relative overflow-hidden bg-white">
                    <CardHeader className="flex flex-row items-start justify-between gap-3">
                      <div>
                        <CardTitle className="text-base">{v.name || v.code || "Voucher"}</CardTitle>
                        <div className="mt-1 text-xs text-muted-foreground">{v.code ? `Code: ${v.code}` : "Code unavailable"}</div>
                      </div>
                      {badgeForUses(remaining)}
                    </CardHeader>
                    <CardContent className="space-y-2 text-sm">
                      <div className="text-muted-foreground line-clamp-2">{v.description || "No description."}</div>
                      <div className="grid grid-cols-2 gap-2 text-xs text-muted-foreground">
                        <div>Min order: {money(v.minOrderTotal)}</div>
                        <div>
                          Discount:{" "}
                          {v.discountType === "PERCENT" ? `${toNumber(v.discountValue)}%` : v.discountType === "FREE_SHIPPING" ? "Free ship" : money(v.discountValue)}
                        </div>
                      </div>
                      {id ? (
                        <Button asChild variant="outline" className="mt-2 w-full rounded-md bg-white">
                          <Link to={`/me/vouchers/${id}`}>View details</Link>
                        </Button>
                      ) : null}
                    </CardContent>
                  </Card>
                );
              })}
            </div>
          )}
        </>
      )}
    </div>
  );
}

