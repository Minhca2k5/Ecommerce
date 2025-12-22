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

const paymentStatuses = ["PENDING", "SUCCESS", "FAILED", "REFUNDED", "CANCELED"] as const;

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
      setItems(asArray(res?.content) as AdminPayment[]);
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
      <Card className="border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Payments</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">Search payments and update status.</div>
          </div>
          <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-4">
            <Input value={qOrderId} onChange={(e) => setQOrderId(e.target.value)} placeholder="Order ID" className="rounded-xl" />
            <select value={qStatus} onChange={(e) => setQStatus(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All statuses</option>
              {paymentStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <Input value={qMethod} onChange={(e) => setQMethod(e.target.value)} placeholder="Method" className="rounded-xl" />
            <Input value={qCurrency} onChange={(e) => setQCurrency(e.target.value)} placeholder="Currency" className="rounded-xl" />
          </div>

          <div className="overflow-x-auto rounded-2xl border bg-background/70">
            <table className="min-w-[820px] w-full text-sm">
              <thead className="bg-muted/50 text-xs text-muted-foreground">
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
                      No payments found.
                    </td>
                  </tr>
                ) : (
                  items.map((p) => {
                    const id = getNumber(p, "id") ?? 0;
                    const orderId = getNumber(p, "orderId");
                    const status = getString(p, "status") ?? "-";
                    const method = getString(p, "method") ?? "-";
                    const currency = getString(p, "currency") ?? "VND";
                    const amount = Number(p["amount"] ?? 0);
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-xs text-muted-foreground">{getString(p, "createdAt") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">{orderId ?? "-"}</td>
                        <td className="px-4 py-3">
                          <span className="rounded-full border bg-background/60 px-3 py-1 text-xs">{status}</span>
                        </td>
                        <td className="px-4 py-3">{method}</td>
                        <td className="px-4 py-3">{formatCurrency(amount, currency)}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <select
                              value={status}
                              onChange={(e) => void updateStatus(id, e.target.value)}
                              className="h-9 rounded-xl border bg-background px-3 text-xs"
                              disabled={!id}
                            >
                              {paymentStatuses.map((s) => (
                                <option key={s} value={s}>
                                  {s}
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
            <div className="text-xs text-muted-foreground">
              Page <span className="font-medium text-foreground">{page + 1}</span> / {totalPages}
            </div>
            <div className="flex items-center gap-2">
              <select value={String(size)} onChange={(e) => setSize(Number(e.target.value))} className="h-9 rounded-xl border bg-background px-3 text-sm">
                {[10, 20, 30, 50].map((n) => (
                  <option key={n} value={String(n)}>
                    {n}/page
                  </option>
                ))}
              </select>
              <Button variant="outline" className="h-9 rounded-xl" disabled={page <= 0} onClick={() => setPage((p) => Math.max(0, p - 1))}>
                Prev
              </Button>
              <Button
                variant="outline"
                className="h-9 rounded-xl"
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
          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">
              {formatCurrency(Number((details ?? {})["amount"] ?? 0), getString(details ?? {}, "currency") ?? "VND")}
            </div>
            <div className="mt-1 text-xs text-muted-foreground">
              Order: {getNumber(details ?? {}, "orderId") ?? "-"} • Method: {getString(details ?? {}, "method") ?? "-"}
            </div>
          </div>

          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">Status</div>
            <div className="mt-3 flex flex-wrap items-center gap-2">
              <select
                value={getString(details ?? {}, "status") ?? ""}
                onChange={(e) => {
                  if (detailsId) void updateStatus(detailsId, e.target.value);
                }}
                className="h-10 rounded-xl border bg-background px-3 text-sm"
                disabled={!detailsId}
              >
                {paymentStatuses.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
              <div className="text-xs text-muted-foreground">Updated at: {getString(details ?? {}, "updatedAt") ?? "-"}</div>
            </div>
          </div>

          <div className="flex justify-end">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
