import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminGet } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminVoucherUse = Record<string, unknown>;

type Mode = "search" | "byUser" | "byOrder" | "byVoucher";

export default function AdminVoucherUsesPage() {
  const toast = useToast();
  const [mode, setMode] = useState<Mode>("search");

  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminVoucherUse[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qUserId, setQUserId] = useState("");
  const [qOrderId, setQOrderId] = useState("");
  const [qVoucherId, setQVoucherId] = useState("");

  const query = useMemo(() => buildQuery({ page, size, sort: "id,desc" }), [page, size]);

  async function load() {
    setIsLoading(true);
    try {
      let path = `/api/admin/voucher-uses${query}`;
      if (mode === "byUser") {
        const id = qUserId.trim() ? Number(qUserId) : 0;
        if (!id) throw new Error("Missing userId");
        path = `/api/admin/voucher-uses/user/${id}${query}`;
      } else if (mode === "byOrder") {
        const id = qOrderId.trim() ? Number(qOrderId) : 0;
        if (!id) throw new Error("Missing orderId");
        path = `/api/admin/voucher-uses/order/${id}${query}`;
      } else if (mode === "byVoucher") {
        const id = qVoucherId.trim() ? Number(qVoucherId) : 0;
        if (!id) throw new Error("Missing voucherId");
        path = `/api/admin/voucher-uses/voucher/${id}${query}`;
      }
      const res = await adminGet<PageResponse<AdminVoucherUse>>(path);
      setItems(asArray(res?.content) as AdminVoucherUse[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load voucher uses.") });
      setItems([]);
      setTotalPages(1);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [mode, query]);

  return (
    <Card className="border bg-background/75 shadow-sm backdrop-blur">
      <CardHeader className="flex flex-row items-start justify-between gap-3">
        <div>
          <CardTitle>Voucher uses</CardTitle>
          <div className="mt-1 text-sm text-muted-foreground">Track voucher usage across users and orders.</div>
        </div>
        <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
          Refresh
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid gap-3 md:grid-cols-4">
          <select value={mode} onChange={(e) => setMode(e.target.value as Mode)} className="h-10 rounded-xl border bg-background px-3 text-sm">
            <option value="search">Search (all)</option>
            <option value="byUser">By userId</option>
            <option value="byOrder">By orderId</option>
            <option value="byVoucher">By voucherId</option>
          </select>
          <Input value={qUserId} onChange={(e) => setQUserId(e.target.value)} placeholder="userId" className="rounded-xl" disabled={mode !== "byUser"} />
          <Input value={qOrderId} onChange={(e) => setQOrderId(e.target.value)} placeholder="orderId" className="rounded-xl" disabled={mode !== "byOrder"} />
          <Input value={qVoucherId} onChange={(e) => setQVoucherId(e.target.value)} placeholder="voucherId" className="rounded-xl" disabled={mode !== "byVoucher"} />
        </div>

        <div className="overflow-hidden rounded-2xl border">
          <table className="w-full text-sm">
            <thead className="bg-muted/50 text-xs text-muted-foreground">
              <tr>
                <th className="px-4 py-3 text-left font-medium">Use</th>
                <th className="px-4 py-3 text-left font-medium">Voucher</th>
                <th className="px-4 py-3 text-left font-medium">User</th>
                <th className="px-4 py-3 text-left font-medium">Order</th>
                <th className="px-4 py-3 text-left font-medium">Discount</th>
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
                    No voucher uses found.
                  </td>
                </tr>
              ) : (
                items.map((u, idx) => (
                  <tr key={String(getNumber(u, "id") ?? idx)} className="border-t">
                    <td className="px-4 py-3">
                      <div className="font-medium">#{getNumber(u, "id") ?? "-"}</div>
                      <div className="text-xs text-muted-foreground">{getString(u, "createdAt") ?? ""}</div>
                    </td>
                    <td className="px-4 py-3">{getNumber(u, "voucherId") ?? "-"}</td>
                    <td className="px-4 py-3">{getNumber(u, "userId") ?? "-"}</td>
                    <td className="px-4 py-3">{getNumber(u, "orderId") ?? "-"}</td>
                    <td className="px-4 py-3">{String(u["discountAmount"] ?? "-")}</td>
                  </tr>
                ))
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
  );
}
