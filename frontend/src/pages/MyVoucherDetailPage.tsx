import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/errors";
import { formatCurrency } from "@/lib/format";
import { getMyVoucherById, type VoucherResponse } from "@/lib/voucherApi";

function toNumber(value: number | string | undefined) {
  if (value === undefined || value === null) return 0;
  const n = typeof value === "string" ? Number(value) : value;
  return Number.isFinite(n) ? n : 0;
}

function money(value: number | string | undefined, currency = "VND") {
  return formatCurrency(toNumber(value), currency);
}

export default function MyVoucherDetailPage() {
  const { voucherId } = useParams();
  const id = useMemo(() => Number(voucherId ?? 0), [voucherId]);
  const [item, setItem] = useState<VoucherResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);
    if (!id) {
      setError("Invalid voucher id.");
      setIsLoading(false);
      return;
    }
    getMyVoucherById(id)
      .then((v) => alive && setItem(v ?? null))
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load voucher.")))
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, [id]);

  if (isLoading) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (error) {
    return <EmptyState title="Couldn't load voucher" description={error} action={<Button asChild className="h-10 rounded-md bg-primary text-primary-foreground"><Link to="/me/vouchers">Back</Link></Button>} />;
  }

  if (!item) {
    return <EmptyState title="Voucher not found" description="This voucher is not available for your account." action={<Button asChild variant="outline" className="h-10 rounded-md bg-background"><Link to="/me/vouchers">Back</Link></Button>} />;
  }

  const discountLabel =
    item.discountType === "PERCENT"
      ? `${toNumber(item.discountValue)}% (max ${money(item.maxDiscountAmount)})`
      : item.discountType === "FREE_SHIPPING"
        ? "Free shipping"
        : money(item.discountValue);

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-sm text-muted-foreground">My vouchers</div>
            <div className="text-2xl font-semibold">{item.name || item.code || "Voucher"}</div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-md bg-background">
            <Link to="/me/vouchers">Back</Link>
          </Button>
        </div>
      </section>

      <div className="grid gap-4 lg:grid-cols-3">
        <Card className="bg-background lg:col-span-2">
          <CardHeader>
            <CardTitle className="text-base">Details</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="text-muted-foreground">{item.description || "No description."}</div>
            <div className="grid gap-2 sm:grid-cols-2">
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">Discount</div>
                <div className="mt-1 font-medium">{discountLabel}</div>
              </div>
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">Min order total</div>
                <div className="mt-1 font-medium">{money(item.minOrderTotal)}</div>
              </div>
            </div>
            <div className="grid gap-2 sm:grid-cols-2">
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">Valid from</div>
                <div className="mt-1 font-medium">{item.startAt || "-"}</div>
              </div>
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">Valid until</div>
                <div className="mt-1 font-medium">{item.endAt || "-"}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className="bg-background">
          <CardHeader>
            <CardTitle className="text-base">Availability</CardTitle>
          </CardHeader>
          <CardContent className="space-y-3 text-sm">
            <div className="rounded-md border bg-background p-4">
              <div className="text-xs font-medium text-muted-foreground">Remaining uses</div>
              <div className="mt-1 text-lg font-semibold">{typeof item.activeUsesForUser === "number" ? item.activeUsesForUser : "-"}</div>
            </div>
            {typeof item.usageLimitUser === "number" ? (
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">Limit per user</div>
                <div className="mt-1 font-medium">{item.usageLimitUser}</div>
              </div>
            ) : null}
            {typeof item.usageLimitGlobal === "number" || typeof item.activeUses === "number" ? (
              <div className="rounded-md border bg-background p-4">
                <div className="text-xs font-medium text-muted-foreground">System remaining</div>
                <div className="mt-1 font-medium">{typeof item.activeUses === "number" ? item.activeUses : "-"}</div>
              </div>
            ) : null}
          </CardContent>
        </Card>
      </div>
    </div>
  );
}

