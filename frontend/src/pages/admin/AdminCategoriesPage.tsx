import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPatch, adminPost } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
  number?: number;
};

type AdminCategory = Record<string, unknown>;

export default function AdminCategoriesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminCategory[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalPages, setTotalPages] = useState(1);

  const [qName, setQName] = useState("");
  const [qSlug, setQSlug] = useState("");
  const [qParentId, setQParentId] = useState<string>("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [details, setDetails] = useState<AdminCategory | null>(null);

  const [form, setForm] = useState({ name: "", slug: "", parentId: "" });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      name: qName.trim() || undefined,
      slug: qSlug.trim() || undefined,
      parentId: qParentId ? Number(qParentId) : undefined,
      sort: "id,asc",
    });
  }, [page, qName, qParentId, qSlug, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminCategory>>(`/api/admin/categories${query}`);
      setItems(asArray(res?.content) as AdminCategory[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load categories.") });
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
    setForm({ name: "", slug: "", parentId: "" });
    setIsFormOpen(true);
  }

  function openEdit(c: AdminCategory) {
    const id = getNumber(c, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      name: getString(c, "name") ?? "",
      slug: getString(c, "slug") ?? "",
      parentId: String(getNumber(c, "parentId") ?? ""),
    });
    setIsFormOpen(true);
  }

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminCategory>(`/api/admin/categories/${id}/details`);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load details.") });
    }
  }

  async function save() {
    const payload = { name: form.name.trim(), slug: form.slug.trim(), parentId: form.parentId ? Number(form.parentId) : null };
    if (!payload.name || !payload.slug) {
      toast.push({ variant: "error", title: "Invalid form", message: "Name and slug are required." });
      return;
    }

    try {
      if (editingId) {
        await adminPatch(`/api/admin/categories/${editingId}${buildQuery({ name: payload.name, slug: payload.slug })}`);
        toast.push({ variant: "success", title: "Updated", message: "Category updated." });
      } else {
        await adminPost(`/api/admin/categories`, payload);
        toast.push({ variant: "success", title: "Created", message: "Category created." });
      }
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(e, "Failed to save category.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/categories/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Category deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete category.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Categories</CardTitle>
          </div>
          <Button className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
            New category
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qName} onChange={(e) => setQName(e.target.value)} placeholder="Search name..." className="rounded-xl" />
            <Input value={qSlug} onChange={(e) => setQSlug(e.target.value)} placeholder="Slug..." className="rounded-xl" />
            <Input value={qParentId} onChange={(e) => setQParentId(e.target.value)} placeholder="Parent ID..." className="rounded-xl" />
          </div>

          <div className="table-shell">
            <table className="min-w-[760px] w-full text-sm">
              <thead className="bg-muted/40 text-left text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3">Category</th>
                  <th className="px-4 py-3">Slug</th>
                  <th className="px-4 py-3">Parent</th>
                  <th className="px-4 py-3 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  <tr>
                    <td className="px-4 py-6 text-muted-foreground" colSpan={4}>
                      Loading...
                    </td>
                  </tr>
                ) : items.length === 0 ? (
                  <tr>
                    <td className="px-4 py-6 text-muted-foreground" colSpan={4}>
                      No categories found.
                    </td>
                  </tr>
                ) : (
                  items.map((c) => {
                    const id = getNumber(c, "id") ?? 0;
                    const name = getString(c, "name") ?? "Category";
                    const slug = getString(c, "slug") ?? "";
                    const parentId = getNumber(c, "parentId");
                    return (
                      <tr key={String(id)} className="border-t hover:bg-muted/20">
                        <td className="px-4 py-3">
                          <div className="font-medium">{name}</div>
                        </td>
                        <td className="px-4 py-3">{slug}</td>
                        <td className="px-4 py-3">{parentId ? `#${parentId}` : "-"}</td>
                        <td className="px-4 py-3 text-right">
                          <div className="inline-flex gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openDetails(id)}>
                              Details
                            </Button>
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openEdit(c)}>
                              Edit
                            </Button>
                            <Button
                              variant="outline"
                              className="h-9 rounded-xl action-danger"
                              onClick={() => setDeleteId(id)}
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
            <div />
            <div className="flex items-center gap-2">
              <select value={String(size)} onChange={(e) => setSize(Number(e.target.value))} className="h-9 rounded-xl border bg-background px-3 text-sm">
                {[10, 20, 50, 100].map((n) => (
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

      <Modal isOpen={isFormOpen} title={editingId ? `Edit category #${editingId}` : "New category"} onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <Input value={form.name} onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))} placeholder="Name *" className="rounded-xl" />
          <Input value={form.slug} onChange={(e) => setForm((f) => ({ ...f, slug: e.target.value }))} placeholder="Slug *" className="rounded-xl" />
          <Input value={form.parentId} onChange={(e) => setForm((f) => ({ ...f, parentId: e.target.value }))} placeholder="Parent ID (optional)" className="rounded-xl" />
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

      <Modal isOpen={isDetailsOpen} title="Category details" onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-4">
          <div className="rounded-xl border bg-background p-4">
            <div className="text-sm font-semibold">{getString(details, "name") ?? "Category"}</div>
            <div className="mt-1 text-sm text-muted-foreground">{getString(details, "slug") ?? "-"}</div>
          </div>
          <div>
            <div className="text-sm font-semibold">Subcategories</div>
            <div className="mt-2 flex flex-wrap gap-2">
              {asArray((details ?? {})["subcategories"]).length ? (
                asArray((details ?? {})["subcategories"]).map((s) => {
                  const id = getNumber(s as any, "id") ?? 0;
                  const name = getString(s as any, "name") ?? "Sub";
                  return (
                    <span key={String(id)} className="rounded-full border bg-background px-3 py-1 text-sm">
                      {name}
                    </span>
                  );
                })
              ) : (
                <div className="text-sm text-muted-foreground">No subcategories.</div>
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

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete category?"
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
