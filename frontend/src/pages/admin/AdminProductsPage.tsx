import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPost, adminPut } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { getErrorMessage } from "@/lib/errors";
import { useToast } from "@/app/ToastProvider";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
};

type AdminProduct = Record<string, unknown>;
type AdminCategory = Record<string, unknown>;

const productStatuses = ["ACTIVE", "INACTIVE", "OUT_OF_STOCK"] as const;

function formatVnd(value: unknown) {
  const n = Number(value ?? 0);
  if (!Number.isFinite(n)) return "0 đ";
  return `${n.toLocaleString("vi-VN")} đ`;
}

export default function AdminProductsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminProduct[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qName, setQName] = useState("");
  const [qSku, setQSku] = useState("");
  const [qStatus, setQStatus] = useState<string>("");
  const [qCategoryId, setQCategoryId] = useState<string>("");

  const [categories, setCategories] = useState<AdminCategory[]>([]);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [form, setForm] = useState({
    name: "",
    slug: "",
    sku: "",
    description: "",
    price: "",
    currency: "VND",
    status: "ACTIVE",
    categoryId: "",
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      name: qName.trim() || undefined,
      sku: qSku.trim() || undefined,
      status: qStatus || undefined,
      categoryId: qCategoryId ? Number(qCategoryId) : undefined,
      sort: "id,desc",
    });
  }, [page, qCategoryId, qName, qSku, qStatus, size]);

  async function load() {
    setIsLoading(true);
    try {
      const [res, cats] = await Promise.all([
        adminGet<PageResponse<AdminProduct>>(`/api/admin/products${query}`),
        adminGet<PageResponse<AdminCategory>>(`/api/admin/categories${buildQuery({ page: 0, size: 200, sort: "id,asc" })}`),
      ]);
      setItems(asArray(res?.content) as AdminProduct[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
      setCategories(asArray(cats?.content) as AdminCategory[]);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load products.") });
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
      name: "",
      slug: "",
      sku: "",
      description: "",
      price: "",
      currency: "VND",
      status: "ACTIVE",
      categoryId: "",
    });
    setIsFormOpen(true);
  }

  function openEdit(p: AdminProduct) {
    const id = getNumber(p, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      name: getString(p, "name") ?? "",
      slug: getString(p, "slug") ?? "",
      sku: getString(p, "sku") ?? "",
      description: getString(p, "description") ?? "",
      price: String(p["price"] ?? ""),
      currency: getString(p, "currency") ?? "VND",
      status: (getString(p, "status") ?? "ACTIVE") as string,
      categoryId: String(getNumber(p, "categoryId") ?? ""),
    });
    setIsFormOpen(true);
  }

  async function save() {
    const payload = {
      name: form.name.trim(),
      slug: form.slug.trim(),
      sku: form.sku.trim(),
      description: form.description.trim() || null,
      price: Number(form.price),
      currency: form.currency || "VND",
      status: form.status || "ACTIVE",
      categoryId: Number(form.categoryId),
    };

    if (!payload.name || !payload.slug || !payload.sku || !Number.isFinite(payload.price) || payload.price <= 0 || !payload.categoryId) {
      toast.push({ variant: "error", title: "Invalid form", message: "Please fill required fields (name/slug/sku/price/category)." });
      return;
    }

    try {
      if (editingId) {
        await adminPut(`/api/admin/products/${editingId}`, {
          name: payload.name,
          slug: payload.slug,
          sku: payload.sku,
          description: payload.description,
          price: payload.price,
          currency: payload.currency,
        });
        toast.push({ variant: "success", title: "Updated", message: "Product updated." });
      } else {
        await adminPost(`/api/admin/products`, payload);
        toast.push({ variant: "success", title: "Created", message: "Product created." });
      }
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(e, "Failed to save product.") });
    }
  }

  async function updateStatus(id: number, status: string) {
    try {
      await adminPut(`/api/admin/products/${id}/status${buildQuery({ status })}`);
      toast.push({ variant: "success", title: "Updated", message: "Status updated." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update status.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/products/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Product deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete product.") });
    }
  }

  const categoryOptions = useMemo(() => {
    return categories
      .map((c) => ({
        id: getNumber(c, "id") ?? 0,
        name: getString(c, "name") ?? "Category",
      }))
      .filter((c) => c.id > 0);
  }, [categories]);

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Products</CardTitle>
          </div>
          <Button className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
            New product
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-4">
            <Input value={qName} onChange={(e) => setQName(e.target.value)} placeholder="Search name..." className="rounded-xl" />
            <Input value={qSku} onChange={(e) => setQSku(e.target.value)} placeholder="SKU..." className="rounded-xl" />
            <select
              value={qStatus}
              onChange={(e) => setQStatus(e.target.value)}
              className="h-10 rounded-xl border bg-background px-3 text-sm"
            >
              <option value="">All status</option>
              {productStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <select
              value={qCategoryId}
              onChange={(e) => setQCategoryId(e.target.value)}
              className="h-10 rounded-xl border bg-background px-3 text-sm"
            >
              <option value="">All categories</option>
              {categoryOptions.map((c) => (
                <option key={c.id} value={String(c.id)}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="table-shell">
            <table className="min-w-[760px] w-full text-sm">
              <thead className="bg-muted/40 text-left text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3">Product</th>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Price</th>
                  <th className="px-4 py-3">Status</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td className="px-4 py-6 text-muted-foreground" colSpan={5}>
                      Loading...
                    </td>
                  </tr>
                ) : items.length === 0 ? (
                  <tr>
                    <td className="px-4 py-6 text-muted-foreground" colSpan={5}>
                      No products found.
                    </td>
                  </tr>
                ) : (
                  items.map((p) => {
                    const id = getNumber(p, "id") ?? 0;
                    const name = getString(p, "name") ?? "Product";
                    const sku = getString(p, "sku") ?? "";
                    const catName = getString(p, "categoryName") ?? "-";
                    const status = getString(p, "status") ?? "-";
                    return (
                      <tr key={String(id)} className="border-t hover:bg-muted/20">
                        <td className="px-4 py-3">
                          <div className="font-medium">{name}</div>
                          <div className="text-sm text-muted-foreground">{sku}</div>
                        </td>
                        <td className="px-4 py-3">{catName}</td>
                        <td className="px-4 py-3">{formatVnd(p["price"])}</td>
                        <td className="px-4 py-3">
                          <select
                            value={status}
                            onChange={(e) => updateStatus(id, e.target.value)}
                            className="h-9 rounded-xl border bg-background px-3 text-sm"
                          >
                            {productStatuses.map((s) => (
                              <option key={s} value={s}>
                                {s}
                              </option>
                            ))}
                          </select>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <div className="inline-flex gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openEdit(p)}>
                              Edit
                            </Button>
                            <Button variant="outline" className="h-9 rounded-xl action-danger" onClick={() => setDeleteId(id)}>
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

      <Modal
        isOpen={isFormOpen}
        title={editingId ? `Edit product #${editingId}` : "New product"}
        onClose={() => setIsFormOpen(false)}
      >
        <div className="grid gap-3">
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name *" className="rounded-xl" />
            <Input value={form.slug} onChange={(e) => setForm((f) => ({ ...f, slug: e.target.value }))} placeholder="Slug *" className="rounded-xl" />
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.sku} onChange={(e) => setForm((f) => ({ ...f, sku: e.target.value }))} placeholder="SKU *" className="rounded-xl" />
            <Input value={form.price} onChange={(e) => setForm((f) => ({ ...f, price: e.target.value }))} placeholder="Price *" className="rounded-xl" />
          </div>
          <textarea
            value={form.description}
            onChange={(e) => setForm((f) => ({ ...f, description: e.target.value }))}
            placeholder="Description"
            className="min-h-24 w-full rounded-xl border bg-background px-3 py-2 text-sm"
          />
          <div className="grid gap-3 md:grid-cols-3">
            <select value={form.currency} onChange={(e) => setForm((f) => ({ ...f, currency: e.target.value }))} className="h-10 rounded-xl border bg-background px-3 text-sm">
              {["VND", "USD", "EUR"].map((c) => (
                <option key={c} value={c}>
                  {c}
                </option>
              ))}
            </select>
            <select value={form.status} onChange={(e) => setForm((f) => ({ ...f, status: e.target.value }))} className="h-10 rounded-xl border bg-background px-3 text-sm">
              {productStatuses.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <select
              value={form.categoryId}
              onChange={(e) => setForm((f) => ({ ...f, categoryId: e.target.value }))}
              className="h-10 rounded-xl border bg-background px-3 text-sm"
            >
              <option value="">Category *</option>
              {categoryOptions.map((c) => (
                <option key={c.id} value={String(c.id)}>
                  {c.name}
                </option>
              ))}
            </select>
          </div>

          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={save}>
              Save
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete product?"
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
