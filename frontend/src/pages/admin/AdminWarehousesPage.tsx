import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPatch, adminPost, adminPut } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminWarehouse = Record<string, unknown>;

export default function AdminWarehousesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminWarehouse[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qCode, setQCode] = useState("");
  const [qName, setQName] = useState("");
  const [qActive, setQActive] = useState<string>("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminWarehouse | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const [form, setForm] = useState({
    code: "",
    name: "",
    address: "",
    city: "",
    state: "",
    country: "",
    zipcode: "",
    phone: "",
    isActive: true,
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      code: qCode.trim() || undefined,
      name: qName.trim() || undefined,
      isActive: qActive === "" ? undefined : qActive === "true",
      sort: "id,desc",
    });
  }, [page, qActive, qCode, qName, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminWarehouse>>(`/api/admin/warehouses${query}`);
      setItems(asArray(res?.content) as AdminWarehouse[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load warehouses.") });
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
    setForm({ code: "", name: "", address: "", city: "", state: "", country: "", zipcode: "", phone: "", isActive: true });
    setIsFormOpen(true);
  }

  function openEdit(w: AdminWarehouse) {
    const id = getNumber(w, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      code: getString(w, "code") ?? "",
      name: getString(w, "name") ?? "",
      address: getString(w, "address") ?? "",
      city: getString(w, "city") ?? "",
      state: getString(w, "state") ?? "",
      country: getString(w, "country") ?? "",
      zipcode: getString(w, "zipcode") ?? "",
      phone: getString(w, "phone") ?? "",
      isActive: getBoolean(w, "isActive") ?? true,
    });
    setIsFormOpen(true);
  }

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminWarehouse>(`/api/admin/warehouses/${id}/details`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load warehouse details.") });
    }
  }

  async function save() {
    const payload = {
      code: form.code.trim() || null,
      name: form.name.trim() || null,
      address: form.address.trim() || null,
      city: form.city.trim() || null,
      state: form.state.trim() || null,
      country: form.country.trim() || null,
      zipcode: form.zipcode.trim() || null,
      phone: form.phone.trim() || null,
      isActive: Boolean(form.isActive),
    };

    if (!editingId && (!payload.code || !payload.name || !payload.city || !payload.country)) {
      toast.push({ variant: "error", title: "Missing required fields", message: "Code, name, city, and country are required for a new warehouse." });
      return;
    }

    try {
      if (editingId) {
        await adminPut(`/api/admin/warehouses/${editingId}`, payload);
        toast.push({ variant: "success", title: "Updated", message: "Warehouse updated." });
      } else {
        await adminPost(`/api/admin/warehouses`, payload);
        toast.push({ variant: "success", title: "Created", message: "Warehouse created." });
      }
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(e, "Failed to save warehouse.") });
    }
  }

  async function toggleActive(id: number, active: boolean) {
    try {
      await adminPatch(`/api/admin/warehouses/${id}/status${buildQuery({ active })}`);
      toast.push({ variant: "success", title: "Updated", message: "Warehouse status updated." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update status.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/warehouses/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Warehouse deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete warehouse.") });
    }
  }

  function formatAddress(w: AdminWarehouse) {
    const parts = [getString(w, "address"), getString(w, "city"), getString(w, "state"), getString(w, "country")].filter(Boolean);
    return parts.length ? parts.join(", ") : "-";
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Warehouses</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
              New warehouse
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qCode} onChange={(e) => setQCode(e.target.value)} placeholder="Search warehouse code" className="rounded-md" />
            <Input value={qName} onChange={(e) => setQName(e.target.value)} placeholder="Search warehouse name" className="rounded-md" />
            <select title="Select option" value={qActive} onChange={(e) => setQActive(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All statuses</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
          </div>

          <div className="table-shell">
            <table className="min-w-[760px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Warehouse</th>
                  <th className="px-4 py-3 text-left font-medium">Address</th>
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
                      No warehouses match your current filters.
                    </td>
                  </tr>
                ) : (
                  items.map((w) => {
                    const id = getNumber(w, "id") ?? 0;
                    const isActive = getBoolean(w, "isActive") ?? false;
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{getString(w, "name") ?? "-"}</div>
                          <div className="text-sm text-muted-foreground">
                            {getString(w, "code") ?? "-"}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-muted-foreground">{formatAddress(w)}</td>
                        <td className="px-4 py-3">
                          <span className={["rounded-full border px-3 py-1 text-sm", isActive ? "bg-emerald-500/10 text-emerald-700" : "bg-rose-500/10 text-rose-700"].join(" ")}>
                            {isActive ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => openEdit(w)} disabled={!id}>
                              Edit
                            </Button>
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => void toggleActive(id, !isActive)} disabled={!id}>
                              {isActive ? "Deactivate" : "Activate"}
                            </Button>
                            <Button
                              variant="outline"
                              className="h-9 rounded-md action-danger"
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

      <Modal isOpen={isFormOpen} title={editingId ? `Edit warehouse ${form.name || ""}`.trim() : "New warehouse"} onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.code} onChange={(e) => setForm((f) => ({ ...f, code: e.target.value }))} placeholder="Code *" className="rounded-md" disabled={Boolean(editingId)} />
            <Input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name *" className="rounded-md" />
          </div>
          <Input value={form.address} onChange={(e) => setForm((f) => ({ ...f, address: e.target.value }))} placeholder="Address" className="rounded-md" />
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.city} onChange={(e) => setForm((f) => ({ ...f, city: e.target.value }))} placeholder="City *" className="rounded-md" />
            <Input value={form.country} onChange={(e) => setForm((f) => ({ ...f, country: e.target.value }))} placeholder="Country *" className="rounded-md" />
          </div>
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={form.state} onChange={(e) => setForm((f) => ({ ...f, state: e.target.value }))} placeholder="State" className="rounded-md" />
            <Input value={form.zipcode} onChange={(e) => setForm((f) => ({ ...f, zipcode: e.target.value }))} placeholder="Zipcode" className="rounded-md" />
            <Input value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} placeholder="Phone" className="rounded-md" />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.isActive} onChange={(e) => setForm((f) => ({ ...f, isActive: e.target.checked }))} />
            Active
          </label>
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-md" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={save}>
              Save
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isDetailsOpen} title={getString(details ?? {}, "name") ?? (detailsId ? "Warehouse details" : "Warehouse")} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">{getString(details ?? {}, "name") ?? "-"}</div>
            <div className="mt-1 text-sm text-muted-foreground">{formatAddress(details ?? {})}</div>
          </div>
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">Inventories</div>
            <div className="mt-2 text-sm text-muted-foreground">Inventory details appear here when provided by the backend response.</div>
          </div>
          <div className="flex justify-end">
            <Button variant="outline" className="rounded-md" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete warehouse?"
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


