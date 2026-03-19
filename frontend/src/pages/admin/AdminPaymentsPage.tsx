import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import { adminGet, adminPatch } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";
import { formatCurrency } from "@/lib/format";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminPayment = Record<string, unknown>;

function dedupePayments(rows: AdminPayment[]) {
  const map = new Map<number, AdminPayment>();
  for (const row of rows) {
    const id = getNumber(row, "id");
    if (!id) continue;
    if (!map.has(id)) map.set(id, row);
  }
  return Array.from(map.values());
}

const paymentStatuses = ["INITIATED", "SUCCEEDED", "FAILED"] as const;

const paymentStatusMeta: Record<string, { label: string; badgeClass: string }> = {
  INITIATED: { label: "Pending", badgeClass: "border-amber-500/30 bg-amber-500/10 text-amber-700" },
  SUCCEEDED: { label: "Paid", badgeClass: "border-emerald-500/30 bg-emerald-500/10 text-emerald-700" },
  FAILED: { label: "Failed", badgeClass: "border-rose-500/30 bg-rose-500/10 text-rose-700" },
};

function getPaymentStatusMeta(status: string) {
  return paymentStatusMeta[status] ?? { label: "Unknown", badgeClass: "border bg-background text-muted-foreground" };
}

function normalizePaymentStatus(raw: string) {
  const value = (raw || "").toUpperCase();
  if (value === "SUCCESS") return "SUCCEEDED";
  if (value === "PENDING") return "INITIATED";
  if (value === "CANCELED" || value === "CANCELLED" || value === "REFUNDED") return "FAILED";
  return paymentStatuses.includes(value as (typeof paymentStatuses)[number]) ? value : "INITIATED";
}

function canAdminChangePaymentStatus(status: string) {
  return normalizePaymentStatus(status) === "INITIATED";
}

export default function AdminPaymentsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminPayment[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qOrderId, setQOrderId] = useState("");
  const [qStatus, setQStatus] = useState("");
  const [qMethod, setQMethod] = useState("");
  const [qCurrency, setQCurrency] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminPayment | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      orderId: qOrderId.trim() ? Number(qOrderId) : undefined,
      status: qStatus || undefined,
      method: qMethod.trim() || undefined,
      currency: qCurrency.trim() || undefined,
      sort: "id,desc",
    });
  }, [page, qCurrency, qMethod, qOrderId, qStatus, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminPayment>>(`/api/admin/payments${query}`);
      setItems(dedupePayments(asArray(res?.content) as AdminPayment[]));
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load payments.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminPayment>(`/api/admin/payments/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load payment details.") });
    }
  }

  async function updateStatus(paymentId: number, status: string) {
    try {
      await adminPatch(`/api/admin/payments/${paymentId}/status${buildQuery({ status })}`);
      toast.push({ variant: "success", title: "Updated", message: "Payment status updated." });
      await load();
      if (isDetailsOpen && detailsId === paymentId) await openDetails(paymentId);
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update status.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Payments</CardTitle>
          </div>
          <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-4">
            <Input value={qOrderId} onChange={(e) => setQOrderId(e.target.value)} placeholder="Order ID" className="rounded-md" />
            <select aria-label="Filter payment status" title="Filter payment status" value={qStatus} onChange={(e) => setQStatus(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All statuses</option>
              {paymentStatuses.map((s) => (
                <option key={s} value={s}>
                  {getPaymentStatusMeta(s).label}
                </option>
              ))}
            </select>
            <Input value={qMethod} onChange={(e) => setQMethod(e.target.value)} placeholder="Method" className="rounded-md" />
            <Input value={qCurrency} onChange={(e) => setQCurrency(e.target.value)} placeholder="Currency" className="rounded-md" />
          </div>

          <div className="table-shell">
            <table className="min-w-[820px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Payment</th>
                  <th className="px-4 py-3 text-left font-medium">Order</th>
                  <th className="px-4 py-3 text-left font-medium">Status</th>
                  <th className="px-4 py-3 text-left font-medium">Method</th>
                  <th className="px-4 py-3 text-left font-medium">Amount</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={6}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={6}>
                      No payments match your current filters.
                    </td>
                  </tr>
                ) : (
                  items.map((p) => {
                    const id = getNumber(p, "id") ?? 0;
                    const orderId = getNumber(p, "orderId");
                    const status = normalizePaymentStatus(getString(p, "status") ?? "");
                    const statusMeta = getPaymentStatusMeta(status);
                    const method = getString(p, "method") ?? "-";
                    const currency = getString(p, "currency") ?? "VND";
                    const amount = Number(p["amount"] ?? 0);
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground">{getString(p, "createdAt") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">{orderId ?? "-"}</td>
                        <td className="px-4 py-3">
                          <span className={["rounded-full border px-3 py-1 text-sm", statusMeta.badgeClass].join(" ")}>{statusMeta.label}</span>
                        </td>
                        <td className="px-4 py-3">{method}</td>
                        <td className="px-4 py-3">{formatCurrency(amount, currency)}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <select
                              aria-label={`Update status for payment ${id}`}
                              title={`Update status for payment ${id}`}
                              value={status}
                              onChange={(e) => void updateStatus(id, e.target.value)}
                              className="h-9 rounded-md border bg-background px-3 text-sm"
                              disabled={!id || !canAdminChangePaymentStatus(status)}
                            >
                              {paymentStatuses.map((s) => (
                                <option key={s} value={s}>
                                  {getPaymentStatusMeta(s).label}
                                </option>
                              ))}
                            </select>
                          </div>
                        </td>
                      </tr>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between gap-3">
            <div className="text-sm text-muted-foreground">
              Page <span className="font-medium text-foreground">{page + 1}</span> / {totalPages}
            </div>
            <div className="flex items-center gap-2">
              <select aria-label="Payments page size" title="Payments page size" value={String(size)} onChange={(e) => setSize(Number(e.target.value))} className="h-9 rounded-md border bg-background px-3 text-sm">
                {[10, 20, 30, 50].map((n) => (
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
          </div>
        </CardContent>
      </Card>

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Payment #${detailsId}` : "Payment"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">
              {formatCurrency(Number((details ?? {})["amount"] ?? 0), getString(details ?? {}, "currency") ?? "VND")}
            </div>
            <div className="mt-1 text-sm text-muted-foreground">
              Order: {getNumber(details ?? {}, "orderId") ?? "-"} · Method: {getString(details ?? {}, "method") ?? "-"}
            </div>
          </div>

          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">Status</div>
            <div className="mt-3 flex flex-wrap items-center gap-2">
              <span
                className={[
                  "rounded-full border px-3 py-1 text-sm",
                  getPaymentStatusMeta(normalizePaymentStatus(getString(details ?? {}, "status") ?? "")).badgeClass,
                ].join(" ")}
              >
                {getPaymentStatusMeta(normalizePaymentStatus(getString(details ?? {}, "status") ?? "")).label}
              </span>
              <select
                aria-label="Payment status quick update"
                title="Payment status quick update"
                value={normalizePaymentStatus(getString(details ?? {}, "status") ?? "")}
                onChange={(e) => {
                  if (detailsId) void updateStatus(detailsId, e.target.value);
                }}
                className="h-10 rounded-md border bg-background px-3 text-sm"
                disabled={!detailsId || !canAdminChangePaymentStatus(getString(details ?? {}, "status") ?? "")}
              >
                {paymentStatuses.map((s) => (
                  <option key={s} value={s}>
                    {getPaymentStatusMeta(s).label}
                  </option>
                ))}
              </select>
              <div className="text-sm text-muted-foreground">
                {canAdminChangePaymentStatus(getString(details ?? {}, "status") ?? "")
                  ? `Updated at: ${getString(details ?? {}, "updatedAt") ?? "-"}`
                  : "Only pending payments can be changed."}
              </div>
            </div>
          </div>

          <div className="flex justify-end">
            <Button variant="outline" className="rounded-md" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}

