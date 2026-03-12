import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { listMyVoucherUses, listMyVoucherUsesByOrder, listMyVoucherUsesByVoucher, type VoucherUseResponse } from "@/lib/voucherUseApi";

function money(value: number | string | undefined) {
  if (value === undefined || value === null) return "-";
  const n = typeof value === "string" ? Number(value) : value;
  if (!Number.isFinite(n)) return "-";
  return formatCurrency(n, "VND");
}

export default function VoucherUsesPage() {
  const [items, setItems] = useState<VoucherUseResponse[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [orderIdFilter, setOrderIdFilter] = useState("");
  const [voucherIdFilter, setVoucherIdFilter] = useState("");

  const title = useMemo(() => {
    if (orderIdFilter.trim()) return "Voucher uses (filtered)";
    if (voucherIdFilter.trim()) return "Voucher uses (filtered)";
    return "Voucher uses";
  }, [orderIdFilter, voucherIdFilter]);

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const orderId = orderIdFilter.trim() ? Number(orderIdFilter.trim()) : 0;
      const voucherId = voucherIdFilter.trim() ? Number(voucherIdFilter.trim()) : 0;
      const page =
        orderId > 0
          ? await listMyVoucherUsesByOrder(orderId, { page: 0, size: 20 })
          : voucherId > 0
            ? await listMyVoucherUsesByVoucher(voucherId, { page: 0, size: 20 })
            : await listMyVoucherUses({ page: 0, size: 20 });
      setItems(page.content ?? []);
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load voucher uses."));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (error) {
    return (
      <EmptyState
        title="Couldn't load voucher uses"
        description={error}
        action={
          <Button onClick={refresh} className="h-10 rounded-xl bg-primary text-primary-foreground">
            Retry
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-8">
      <section className="page-hero">
        <div className="hero-orb hero-orb--a" />
        <div className="hero-orb hero-orb--b" />
        <div className="relative z-10 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div className="max-w-2xl space-y-2">
            <div className="text-xs font-semibold uppercase tracking-[0.2em] text-primary">Vouchers</div>
            <div className="text-3xl font-semibold tracking-tight">{title}</div>
            <p className="text-sm text-muted-foreground">
              Filter by order or voucher to review your usage history.
            </p>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-xl bg-white/80">
            <Link to="/me/vouchers">My vouchers</Link>
          </Button>
        </div>
      </section>

      <Card className="panel-card">
        <CardHeader>
          <CardTitle className="text-base">Filters</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-3">
          <div className="space-y-2">
            <div className="text-xs font-medium text-muted-foreground">Order ID</div>
            <Input className="rounded-xl bg-white/80" value={orderIdFilter} onChange={(e) => setOrderIdFilter(e.target.value)} inputMode="numeric" placeholder="e.g. 30" />
          </div>
          <div className="space-y-2">
            <div className="text-xs font-medium text-muted-foreground">Voucher ID</div>
            <Input className="rounded-xl bg-white/80" value={voucherIdFilter} onChange={(e) => setVoucherIdFilter(e.target.value)} inputMode="numeric" placeholder="e.g. 1" />
          </div>
          <div className="flex items-end gap-2">
            <Button className="h-10 rounded-xl bg-primary text-primary-foreground" onClick={refresh}>
              Apply
            </Button>
            <Button
              variant="outline"
              className="h-10 rounded-xl bg-white/80"
              onClick={() => {
                setOrderIdFilter("");
                setVoucherIdFilter("");
              }}
            >
              Reset
            </Button>
          </div>
        </CardContent>
      </Card>

      {items.length === 0 ? (
        <EmptyState title="No voucher uses" description="No voucher usage history found for the current filter." />
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {items.map((u) => (
            <Card key={String(u.id)} className="pressable overflow-hidden bg-white/90">
              <div className="h-1 w-full bg-gradient-to-r from-primary/60 via-sky-400/50 to-amber-300/40" />
              <CardHeader className="flex flex-row items-start justify-between gap-3">
                <div>
                  <CardTitle className="text-base">Voucher use</CardTitle>
                  <div className="mt-1 text-sm text-muted-foreground">Discount: {money(u.discountAmount)}</div>
                </div>
                {u.orderId ? (
                  <Button asChild variant="outline" className="rounded-xl bg-white/80">
                    <Link to={`/orders/${u.orderId}`}>View order</Link>
                  </Button>
                ) : null}
              </CardHeader>
            </Card>
          ))}
        </div>
      )}
    </div>
  );
}
