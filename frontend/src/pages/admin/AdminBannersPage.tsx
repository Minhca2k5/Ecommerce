import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import SafeImage from "@/components/SafeImage";
import { adminDelete, adminGet, adminPost, adminPut } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminBanner = Record<string, unknown>;

export default function AdminBannersPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminBanner[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qTitle, setQTitle] = useState("");
  const [qActive, setQActive] = useState<string>("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [form, setForm] = useState({
    title: "",
    imageUrl: "",
    targetUrl: "",
    position: "",
    isActive: true,
    startAt: "",
    endAt: "",
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      title: qTitle.trim() || undefined,
      isActive: qActive === "" ? undefined : qActive === "true",
      sort: "id,desc",
    });
  }, [page, qActive, qTitle, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminBanner>>(`/api/admin/banners${query}`);
      setItems(asArray(res?.content) as AdminBanner[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load banners.") });
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
    setForm({ title: "", imageUrl: "", targetUrl: "", position: "", isActive: true, startAt: "", endAt: "" });
    setIsFormOpen(true);
  }

  function openEdit(b: AdminBanner) {
    const id = getNumber(b, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      title: getString(b, "title") ?? "",
      imageUrl: getString(b, "imageUrl") ?? "",
      targetUrl: getString(b, "targetUrl") ?? "",
      position: String(getNumber(b, "position") ?? ""),
      isActive: getBoolean(b, "isActive") ?? true,
      startAt: getString(b, "startAt") ?? "",
      endAt: getString(b, "endAt") ?? "",
    });
    setIsFormOpen(true);
  }

  async function save() {
    const payload = {
      title: form.title.trim(),
      imageUrl: form.imageUrl.trim(),
      targetUrl: form.targetUrl.trim() || null,
      position: form.position.trim() ? Number(form.position) : null,
      isActive: Boolean(form.isActive),
      startAt: form.startAt.trim() || null,
      endAt: form.endAt.trim() || null,
    };

    if (!payload.title || !payload.imageUrl) {
      toast.push({ variant: "error", title: "Invalid form", message: "Title and image URL are required." });
      return;
    }

    try {
      if (editingId) {
        await adminPut(`/api/admin/banners/${editingId}`, payload);
        toast.push({ variant: "success", title: "Updated", message: "Banner updated." });
      } else {
        await adminPost(`/api/admin/banners`, payload);
        toast.push({ variant: "success", title: "Created", message: "Banner created." });
      }
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(e, "Failed to save banner.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/banners/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Banner deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete banner.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Banners</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
              New banner
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qTitle} onChange={(e) => setQTitle(e.target.value)} placeholder="Title contains" className="rounded-md" />
            <select title="Select option" value={qActive} onChange={(e) => setQActive(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All</option>
              <option value="true">Active</option>
              <option value="false">Inactive</option>
            </select>
            <div />
          </div>

          <div className="table-shell">
            <table className="min-w-[900px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Banner</th>
                  <th className="px-4 py-3 text-left font-medium">Preview</th>
                  <th className="px-4 py-3 text-left font-medium">Active</th>
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
                      No banners found.
                    </td>
                  </tr>
                ) : (
                  items.map((b) => {
                    const id = getNumber(b, "id") ?? 0;
                    const active = getBoolean(b, "isActive") ?? false;
                    const imageUrl = getString(b, "imageUrl") ?? "";
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{getString(b, "title") ?? "-"}</div>
                          <div className="text-sm text-muted-foreground">
                            Position: {getNumber(b, "position") ?? "-"}
                          </div>
                        </td>
                        <td className="px-4 py-3">
                          <SafeImage src={imageUrl} alt="banner" fallbackKey={id} className="h-12 w-24 rounded-md border object-cover" />
                        </td>
                        <td className="px-4 py-3">
                          <span className={["rounded-full border px-3 py-1 text-sm", active ? "bg-emerald-500/10 text-emerald-700" : "bg-rose-500/10 text-rose-700"].join(" ")}>
                            {active ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => openEdit(b)} disabled={!id}>
                              Edit
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

      <Modal isOpen={isFormOpen} title={editingId ? `Edit banner ${form.title || ""}`.trim() : "New banner"} onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <Input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} placeholder="Title *" className="rounded-md" />
          <Input value={form.imageUrl} onChange={(e) => setForm((f) => ({ ...f, imageUrl: e.target.value }))} placeholder="Image URL *" className="rounded-md" />
          <Input value={form.targetUrl} onChange={(e) => setForm((f) => ({ ...f, targetUrl: e.target.value }))} placeholder="Target URL" className="rounded-md" />
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.position} onChange={(e) => setForm((f) => ({ ...f, position: e.target.value }))} placeholder="Position" className="rounded-md" />
            <label className="flex items-center gap-2 text-sm">
              <input type="checkbox" checked={form.isActive} onChange={(e) => setForm((f) => ({ ...f, isActive: e.target.checked }))} />
              Active
            </label>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.startAt} onChange={(e) => setForm((f) => ({ ...f, startAt: e.target.value }))} placeholder="Start at (string)" className="rounded-md" />
            <Input value={form.endAt} onChange={(e) => setForm((f) => ({ ...f, endAt: e.target.value }))} placeholder="End at (string)" className="rounded-md" />
          </div>
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

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete banner?"
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


