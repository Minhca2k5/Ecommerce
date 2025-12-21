import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPost, adminPut } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminVoucher = Record<string, unknown>;

const voucherStatuses = ["ACTIVE", "INACTIVE", "EXPIRED"] as const;
const discountTypes = ["PERCENT", "FIXED"] as const;

export default function AdminVouchersPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminVoucher[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qCode, setQCode] = useState("");
  const [qName, setQName] = useState("");
  const [qStatus, setQStatus] = useState("");
  const [qDiscountType, setQDiscountType] = useState("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [details, setDetails] = useState<AdminVoucher | null>(null);
  const [detailsId, setDetailsId] = useState<number | null>(null);

  const [form, setForm] = useState({
    code: "",
    name: "",
    description: "",
    discountType: "PERCENT",
    discountValue: "",
    maxDiscountAmount: "",
    minOrderTotal: "",
    startAt: "",
    endAt: "",
    usageLimitGlobal: "",
    usageLimitUser: "",
    status: "ACTIVE",
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      code: qCode.trim() || undefined,
      name: qName.trim() || undefined,
      status: qStatus || undefined,
      discountType: qDiscountType || undefined,
      sort: "id,desc",
    });
  }, [page, qCode, qDiscountType, qName, qStatus, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminVoucher>>(`/api/admin/vouchers${query}`);
      setItems(asArray(res?.content) as AdminVoucher[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load vouchers.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  function openCreate() {
    setEditingId(null);
    setForm({
      code: "",
      name: "",
      description: "",
      discountType: "PERCENT",
      discountValue: "",
      maxDiscountAmount: "",
      minOrderTotal: "",
      startAt: "",
      endAt: "",
      usageLimitGlobal: "",
      usageLimitUser: "",
      status: "ACTIVE",
    });
    setIsFormOpen(true);
  }

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminVoucher>(`/api/admin/vouchers/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load voucher details.") });
    }
  }

  function openEdit(v: AdminVoucher) {
    const id = getNumber(v, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      code: getString(v, "code") ?? "",
      name: getString(v, "name") ?? "",
      description: getString(v, "description") ?? "",
      discountType: getString(v, "discountType") ?? "PERCENT",
      discountValue: String(v["discountValue"] ?? ""),
      maxDiscountAmount: String(v["maxDiscountAmount"] ?? ""),
      minOrderTotal: String(v["minOrderTotal"] ?? ""),
      startAt: getString(v, "startAt") ?? "",
      endAt: getString(v, "endAt") ?? "",
      usageLimitGlobal: String(v["usageLimitGlobal"] ?? ""),
      usageLimitUser: String(v["usageLimitUser"] ?? ""),
      status: getString(v, "status") ?? "ACTIVE",
    });
    setIsFormOpen(true);
  }

  async function save() {
    const payload: Record<string, unknown> = {
      name: form.name.trim() || null,
      description: form.description.trim() || null,
      discountType: form.discountType || null,
      discountValue: form.discountValue.trim() ? Number(form.discountValue) : null,
      maxDiscountAmount: form.maxDiscountAmount.trim() ? Number(form.maxDiscountAmount) : null,
      minOrderTotal: form.minOrderTotal.trim() ? Number(form.minOrderTotal) : null,
      startAt: form.startAt.trim() || null,
      endAt: form.endAt.trim() || null,
      usageLimitGlobal: form.usageLimitGlobal.trim() ? Number(form.usageLimitGlobal) : null,
      usageLimitUser: form.usageLimitUser.trim() ? Number(form.usageLimitUser) : null,
      status: form.status || null,
    };

    if (!editingId) {
      if (!form.code.trim() || !form.name.trim() || !form.discountType || !form.startAt.trim() || !form.endAt.trim()) {
        toast.push({ variant: "error", title: "Invalid form", message: "Code, name, discount type, startAt, endAt are required for new voucher." });
        return;
      }
      payload.code = form.code.trim();
    }

    try {
      if (editingId) {
        await adminPut(`/api/admin/vouchers/${editingId}`, payload);
        toast.push({ variant: "success", title: "Updated", message: "Voucher updated." });
      } else {
        await adminPost(`/api/admin/vouchers`, payload);
        toast.push({ variant: "success", title: "Created", message: "Voucher created." });
      }
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(e, "Failed to save voucher.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/vouchers/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Voucher deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete voucher.") });
    }
  }

  return (
    <>
      <Card className="border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Vouchers</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">Manage vouchers (list/create/update/delete).</div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={openCreate}>
              New voucher
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-4">
            <Input value={qCode} onChange={(e) => setQCode(e.target.value)} placeholder="Code" className="rounded-xl" />
            <Input value={qName} onChange={(e) => setQName(e.target.value)} placeholder="Name" className="rounded-xl" />
            <select value={qStatus} onChange={(e) => setQStatus(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All statuses</option>
              {voucherStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <select value={qDiscountType} onChange={(e) => setQDiscountType(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All types</option>
              {discountTypes.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
          </div>

          <div className="overflow-hidden rounded-2xl border">
            <table className="w-full text-sm">
              <thead className="bg-muted/50 text-xs text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Voucher</th>
                  <th className="px-4 py-3 text-left font-medium">Type</th>
                  <th className="px-4 py-3 text-left font-medium">Status</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={4}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={4}>
                      No vouchers found.
                    </td>
                  </tr>
                ) : (
                  items.map((v) => {
                    const id = getNumber(v, "id") ?? 0;
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{getString(v, "name") ?? "-"}</div>
                          <div className="text-xs text-muted-foreground">
                            {getString(v, "code") ?? "-"} • #{id}
                          </div>
                        </td>
                        <td className="px-4 py-3">{getString(v, "discountType") ?? "-"}</td>
                        <td className="px-4 py-3">
                          <span className="rounded-full border bg-background/60 px-3 py-1 text-xs">{getString(v, "status") ?? "-"}</span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openEdit(v)} disabled={!id}>
                              Edit
                            </Button>
                            <Button
                              variant="outline"
                              className="h-9 rounded-xl text-rose-600 hover:bg-rose-500/10 hover:text-rose-700"
                              onClick={() => setDeleteId(id)}
                              disabled={!id}
                            >
                              Delete
                            </Button>
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

      <Modal isOpen={isFormOpen} title={editingId ? `Edit voucher #${editingId}` : "New voucher"} onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          {!editingId ? <Input value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} placeholder="Code *" className="rounded-xl" /> : null}
          <Input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name *" className="rounded-xl" />
          <textarea
            value={form.description}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            placeholder="Description"
            className="min-h-20 w-full rounded-xl border bg-background px-3 py-2 text-sm"
          />
          <div className="grid gap-3 md:grid-cols-2">
            <select value={form.discountType} onChange={(e) => setForm((f) => ({ ...f, discountType: e.target.value }))} className="h-10 rounded-xl border bg-background px-3 text-sm">
              {discountTypes.map((t) => (
                <option key={t} value={t}>
                  {t}
                </option>
              ))}
            </select>
            <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))} className="h-10 rounded-xl border bg-background px-3 text-sm">
              {voucherStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={form.discountValue} onChange={(e) => setForm((f) => ({ ...f, discountValue: e.target.value }))} placeholder="Discount value" className="rounded-xl" />
            <Input value={form.maxDiscountAmount} onChange={(e) => setForm((f) => ({ ...f, maxDiscountAmount: e.target.value }))} placeholder="Max discount" className="rounded-xl" />
            <Input value={form.minOrderTotal} onChange={(e) => setForm((f) => ({ ...f, minOrderTotal: e.target.value }))} placeholder="Min order total" className="rounded-xl" />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.startAt} onChange={(e) => setForm((f) => ({ ...f, startAt: e.target.value }))} placeholder="Start at * (YYYY-MM-DDTHH:mm:ss)" className="rounded-xl" />
            <Input value={form.endAt} onChange={(e) => setForm((f) => ({ ...f, endAt: e.target.value }))} placeholder="End at * (YYYY-MM-DDTHH:mm:ss)" className="rounded-xl" />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.usageLimitGlobal} onChange={(e) => setForm((f) => ({ ...f, usageLimitGlobal: e.target.value }))} placeholder="Usage limit global" className="rounded-xl" />
            <Input value={form.usageLimitUser} onChange={(e) => setForm((f) => ({ ...f, usageLimitUser: e.target.value }))} placeholder="Usage limit per user" className="rounded-xl" />
          </div>
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={save}>
              Save
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Voucher #${detailsId}` : "Voucher"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">{getString(details ?? {}, "name") ?? "-"}</div>
            <div className="mt-1 text-xs text-muted-foreground">
              Code: {getString(details ?? {}, "code") ?? "-"} • Status: {getString(details ?? {}, "status") ?? "-"}
            </div>
          </div>
          <div className="rounded-2xl border bg-background/60 p-4 text-sm text-muted-foreground">
            {getString(details ?? {}, "description") ?? "No description."}
          </div>
          <div className="flex justify-end">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete voucher?"
        description="This action cannot be undone."
        confirmText="Delete"
        variant="danger"
        onClose={() => setDeleteId(null)}
        onConfirm={() => {
          const id = Number(deleteId ?? 0);
          setDeleteId(null);
          if (id) void remove(id);
        }}
      />
    </>
  );
}
