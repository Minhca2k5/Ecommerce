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

type AdminOrder = Record<string, unknown>;

const orderStatuses = ["PENDING", "PAID", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELED", "FAILED"] as const;

export default function AdminOrdersPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminOrder[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qUserId, setQUserId] = useState("");
  const [qStatus, setQStatus] = useState("");
  const [qCurrency, setQCurrency] = useState("");
  const [qMinTotal, setQMinTotal] = useState("");
  const [qMaxTotal, setQMaxTotal] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminOrder | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [editCurrency, setEditCurrency] = useState("");

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      userId: qUserId.trim() ? Number(qUserId) : undefined,
      status: qStatus || undefined,
      currency: qCurrency.trim() || undefined,
      minTotalAmount: qMinTotal.trim() ? Number(qMinTotal) : undefined,
      maxTotalAmount: qMaxTotal.trim() ? Number(qMaxTotal) : undefined,
      sort: "id,desc",
    });
  }, [page, qCurrency, qMaxTotal, qMinTotal, qStatus, qUserId, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminOrder>>(`/api/admin/orders${query}`);
      setItems(asArray(res?.content) as AdminOrder[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load orders.") });
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
      const res = await adminGet<AdminOrder>(`/api/admin/orders/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setEditCurrency(getString(res ?? {}, "currency") ?? "VND");
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load order details.") });
    }
  }

  async function updateStatus(orderId: number, status: string) {
    try {
      await adminPatch(`/api/admin/orders/${orderId}/status${buildQuery({ status })}`);
      toast.push({ variant: "success", title: "Updated", message: "Order status updated." });
      await load();
      if (isDetailsOpen && detailsId === orderId) await openDetails(orderId);
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update status.") });
    }
  }

  async function updateCurrency(orderId: number, currency: string) {
    const c = currency.trim().toUpperCase();
    if (!c) return;
    try {
      await adminPatch(`/api/admin/orders/${orderId}/currency${buildQuery({ currency: c })}`);
      toast.push({ variant: "success", title: "Updated", message: "Order currency updated." });
      await load();
      if (isDetailsOpen && detailsId === orderId) await openDetails(orderId);
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update currency.") });
    }
  }

  const resolvedDetailsCurrency = getString(details ?? {}, "currency") ?? "VND";

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Orders</CardTitle>
          </div>
          <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-5">
            <Input value={qUserId} onChange={(e) => setQUserId(e.target.value)} placeholder="User ID" className="rounded-xl" />
            <select value={qStatus} onChange={(e) => setQStatus(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All statuses</option>
              {orderStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <Input value={qCurrency} onChange={(e) => setQCurrency(e.target.value)} placeholder="Currency (e.g. VND)" className="rounded-xl" />
            <Input value={qMinTotal} onChange={(e) => setQMinTotal(e.target.value)} placeholder="Min total" className="rounded-xl" />
            <Input value={qMaxTotal} onChange={(e) => setQMaxTotal(e.target.value)} placeholder="Max total" className="rounded-xl" />
          </div>

          <div className="table-shell">
            <table className="min-w-[820px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Order</th>
                  <th className="px-4 py-3 text-left font-medium">User</th>
                  <th className="px-4 py-3 text-left font-medium">Status</th>
                  <th className="px-4 py-3 text-left font-medium">Total</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={5}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={5}>
                      No orders found.
                    </td>
                  </tr>
                ) : (
                  items.map((o) => {
                    const id = getNumber(o, "id") ?? 0;
                    const userId = getNumber(o, "userId");
                    const status = getString(o, "status") ?? "-";
                    const currency = getString(o, "currency") ?? "VND";
                    const total = Number(o["totalAmount"] ?? o["total"] ?? 0);
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground">{getString(o, "createdAt") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">{userId ?? "-"}</td>
                        <td className="px-4 py-3">
                          <span className="rounded-full border bg-background px-3 py-1 text-sm">{status}</span>
                        </td>
                        <td className="px-4 py-3">{formatCurrency(total, currency)}</td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <select
                              value={status}
                              onChange={(e) => void updateStatus(id, e.target.value)}
                              className="h-9 rounded-xl border bg-background px-3 text-sm"
                              disabled={!id}
                            >
                              {orderStatuses.map((s) => (
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
            <div className="text-sm text-muted-foreground">
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

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Order #${detailsId}` : "Order"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <div className="rounded-xl border bg-background p-4">
              <div className="text-sm text-muted-foreground">Status</div>
              <div className="mt-1 text-sm font-semibold">{getString(details ?? {}, "status") ?? "-"}</div>
            </div>
            <div className="rounded-xl border bg-background p-4">
              <div className="text-sm text-muted-foreground">Total</div>
              <div className="mt-1 text-sm font-semibold">
                {formatCurrency(Number((details ?? {})["totalAmount"] ?? (details ?? {})["total"] ?? 0), resolvedDetailsCurrency)}
              </div>
            </div>
            <div className="rounded-xl border bg-background p-4">
              <div className="text-sm text-muted-foreground">Currency</div>
              <div className="mt-1 text-sm font-semibold">{resolvedDetailsCurrency}</div>
            </div>
          </div>

          <div className="rounded-xl border bg-background p-4">
            <div className="text-sm font-semibold">Quick updates</div>
            <div className="mt-3 grid gap-3 md:grid-cols-2">
              <select
                value={getString(details ?? {}, "status") ?? ""}
                onChange={(e) => {
                  if (detailsId) void updateStatus(detailsId, e.target.value);
                }}
                className="h-10 rounded-xl border bg-background px-3 text-sm"
                disabled={!detailsId}
              >
                {orderStatuses.map((s) => (
                  <option key={s} value={s}>
                    {s}
                  </option>
                ))}
              </select>
              <div className="flex gap-2">
                <Input value={editCurrency} onChange={(e) => setEditCurrency(e.target.value)} placeholder="Currency" className="rounded-xl" />
                <Button
                  variant="outline"
                  className="h-10 rounded-xl"
                  onClick={() => {
                    if (!detailsId) return;
                    void updateCurrency(detailsId, editCurrency);
                  }}
                  disabled={!detailsId}
                >
                  Apply
                </Button>
              </div>
            </div>
          </div>

          <div className="rounded-xl border bg-background p-4">
            <div className="text-sm font-semibold">Items</div>
            <div className="mt-2 space-y-2">
              {asArray((details ?? {})["items"]).length ? (
                asArray((details ?? {})["items"]).map((it, idx) => {
                  const name = getString(it as any, "productNameSnapshot") ?? getString(it as any, "productName") ?? "Item";
                  const qty = getNumber(it as any, "quantity") ?? 0;
                  const lineTotal = Number((it as any)["lineTotal"] ?? 0);
                  return (
                    <div key={idx} className="flex items-center justify-between rounded-xl border bg-background/50 px-3 py-2 text-sm">
                      <div className="min-w-0">
                        <div className="truncate font-medium">{name}</div>
                        <div className="text-sm text-muted-foreground">Qty: {qty}</div>
                      </div>
                      <div className="shrink-0 font-medium">{formatCurrency(lineTotal, resolvedDetailsCurrency)}</div>
                    </div>
                  );
                })
              ) : (
                <div className="text-sm text-muted-foreground">No items in response.</div>
              )}
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
