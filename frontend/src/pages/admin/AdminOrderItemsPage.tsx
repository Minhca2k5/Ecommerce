import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import { adminGet } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";
import { formatCurrency } from "@/lib/format";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminOrderItem = Record<string, unknown>;

export default function AdminOrderItemsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminOrderItem[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qOrderId, setQOrderId] = useState("");
  const [qProductId, setQProductId] = useState("");
  const [qProductName, setQProductName] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminOrderItem | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      orderId: qOrderId.trim() ? Number(qOrderId) : undefined,
      productId: qProductId.trim() ? Number(qProductId) : undefined,
      productNameSnapshot: qProductName.trim() || undefined,
      sort: "id,desc",
    });
  }, [page, qOrderId, qProductId, qProductName, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminOrderItem>>(`/api/admin/order-items${query}`);
      setItems(asArray(res?.content) as AdminOrderItem[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load order items.") });
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
      const res = await adminGet<AdminOrderItem>(`/api/admin/order-items/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load details.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Order items</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">Search order items and inspect line details.</div>
          </div>
          <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qOrderId} onChange={(e) => setQOrderId(e.target.value)} placeholder="Order ID" className="rounded-md" />
            <Input value={qProductId} onChange={(e) => setQProductId(e.target.value)} placeholder="Product ID" className="rounded-md" />
            <Input value={qProductName} onChange={(e) => setQProductName(e.target.value)} placeholder="Product name contains" className="rounded-md" />
          </div>

          <div className="table-shell">
            <table className="min-w-[860px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Item</th>
                  <th className="px-4 py-3 text-left font-medium">Order</th>
                  <th className="px-4 py-3 text-left font-medium">Product</th>
                  <th className="px-4 py-3 text-left font-medium">Qty</th>
                  <th className="px-4 py-3 text-left font-medium">Line total</th>
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
                      No items found.
                    </td>
                  </tr>
                ) : (
                  items.map((it) => {
                    const id = getNumber(it, "id") ?? 0;
                    const orderId = getNumber(it, "orderId");
                    const productId = getNumber(it, "productId");
                    const name = getString(it, "productNameSnapshot") ?? getString(it, "productName") ?? "-";
                    const qty = getNumber(it, "quantity") ?? 0;
                    const currency = getString(it, "currency") ?? "VND";
                    const lineTotal = Number(it["lineTotal"] ?? 0);
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground">{name}</div>
                        </td>
                        <td className="px-4 py-3">{orderId ?? "-"}</td>
                        <td className="px-4 py-3">{productId ?? "-"}</td>
                        <td className="px-4 py-3">{qty}</td>
                        <td className="px-4 py-3">{formatCurrency(lineTotal, currency)}</td>
                        <td className="px-4 py-3 text-right">
                          <Button variant="outline" className="h-9 rounded-md" onClick={() => openDetails(id)} disabled={!id}>
                            Details
                          </Button>
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
              <select title="Select option" value={String(size)} onChange={(e) => setSize(Number(e.target.value))} className="h-9 rounded-md border bg-background px-3 text-sm">
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

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Order item #${detailsId}` : "Order item"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">{getString(details ?? {}, "productNameSnapshot") ?? "Item"}</div>
            <div className="mt-1 text-sm text-muted-foreground">
              Order: {getNumber(details ?? {}, "orderId") ?? "-"} • Product: {getNumber(details ?? {}, "productId") ?? "-"}
            </div>
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm text-muted-foreground">Quantity</div>
              <div className="mt-1 text-sm font-semibold">{getNumber(details ?? {}, "quantity") ?? 0}</div>
            </div>
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm text-muted-foreground">Unit price</div>
              <div className="mt-1 text-sm font-semibold">
                {formatCurrency(Number((details ?? {})["priceSnapshot"] ?? (details ?? {})["unitPrice"] ?? 0), getString(details ?? {}, "currency") ?? "VND")}
              </div>
            </div>
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm text-muted-foreground">Line total</div>
              <div className="mt-1 text-sm font-semibold">
                {formatCurrency(Number((details ?? {})["lineTotal"] ?? 0), getString(details ?? {}, "currency") ?? "VND")}
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

