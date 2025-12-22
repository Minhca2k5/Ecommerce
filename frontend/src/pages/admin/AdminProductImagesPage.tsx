import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import SafeImage from "@/components/SafeImage";
import { adminDelete, adminGet, adminPatch, adminPost } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type AdminProductImage = Record<string, unknown>;

export default function AdminProductImagesPage() {
  const toast = useToast();
  const [productId, setProductId] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [items, setItems] = useState<AdminProductImage[]>([]);
  const [primary, setPrimary] = useState<AdminProductImage | null>(null);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [editId, setEditId] = useState<number | null>(null);
  const [editUrl, setEditUrl] = useState("");
  const [isEditOpen, setIsEditOpen] = useState(false);

  const [form, setForm] = useState({ url: "", isPrimary: false });

  async function load() {
    const id = productId.trim() ? Number(productId) : 0;
    if (!id) {
      setItems([]);
      setPrimary(null);
      return;
    }

    setIsLoading(true);
    try {
      const [list, p] = await Promise.all([
        adminGet<AdminProductImage[]>(`/api/admin/product-images/product/${id}`),
        adminGet<AdminProductImage>(`/api/admin/product-images/product/${id}/primary`).catch(() => null as any),
      ]);
      setItems(asArray(list) as AdminProductImage[]);
      setPrimary(p ?? null);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load product images.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setForm({ url: "", isPrimary: false });
    setIsFormOpen(true);
  }

  async function create() {
    const pid = productId.trim() ? Number(productId) : 0;
    const payload = { productId: pid, url: form.url.trim(), isPrimary: Boolean(form.isPrimary) };
    if (!payload.productId || !payload.url) {
      toast.push({ variant: "error", title: "Invalid form", message: "Product ID and URL are required." });
      return;
    }
    try {
      await adminPost(`/api/admin/product-images`, payload);
      toast.push({ variant: "success", title: "Created", message: "Image created." });
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Create failed", message: getErrorMessage(e, "Failed to create image.") });
    }
  }

  function openEdit(img: AdminProductImage) {
    const id = getNumber(img, "id") ?? 0;
    if (!id) return;
    setEditId(id);
    setEditUrl(getString(img, "url") ?? "");
    setIsEditOpen(true);
  }

  async function saveUrl() {
    const id = Number(editId ?? 0);
    const url = editUrl.trim();
    if (!id || !url) return;
    try {
      await adminPatch(`/api/admin/product-images/${id}/url${buildQuery({ url })}`);
      toast.push({ variant: "success", title: "Updated", message: "URL updated." });
      setIsEditOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update URL.") });
    }
  }

  async function setAsPrimary(imageId: number) {
    const pid = productId.trim() ? Number(productId) : 0;
    if (!pid || !imageId) return;
    try {
      await adminPatch(`/api/admin/product-images/${imageId}/primary${buildQuery({ productId: pid })}`);
      toast.push({ variant: "success", title: "Updated", message: "Primary image updated." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to set primary image.") });
    }
  }

  async function remove(imageId: number) {
    try {
      await adminDelete(`/api/admin/product-images/${imageId}`);
      toast.push({ variant: "success", title: "Deleted", message: "Image deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete image.") });
    }
  }

  return (
    <>
      <Card className="border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Product images</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">List images by product, update URL, and set primary.</div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
              Load
            </Button>
            <Button className="h-9 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={openCreate}>
              Add image
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <Input value={productId} onChange={(e) => setProductId(e.target.value)} placeholder="Product ID" className="rounded-xl" />
          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">Primary</div>
            <div className="mt-3 flex items-center gap-3">
              {primary ? (
                <>
                  <SafeImage src={getString(primary, "url") ?? ""} alt="primary" fallbackKey={getNumber(primary, "id") ?? "primary"} className="h-14 w-28 rounded-xl border object-cover" />
                  <div className="text-sm">
                    <div className="font-medium">#{getNumber(primary, "id") ?? "-"}</div>
                    <div className="text-xs text-muted-foreground line-clamp-1">{getString(primary, "url") ?? ""}</div>
                  </div>
                </>
              ) : (
                <div className="text-sm text-muted-foreground">No primary image found.</div>
              )}
            </div>
          </div>

          <div className="overflow-x-auto rounded-2xl border bg-background/70">
            <table className="min-w-[860px] w-full text-sm">
              <thead className="bg-muted/50 text-xs text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Image</th>
                  <th className="px-4 py-3 text-left font-medium">Preview</th>
                  <th className="px-4 py-3 text-left font-medium">Primary</th>
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
                      Enter a productId to list images.
                    </td>
                  </tr>
                ) : (
                  items.map((img) => {
                    const id = getNumber(img, "id") ?? 0;
                    const url = getString(img, "url") ?? "";
                    const isPrimary = getBoolean(img, "isPrimary") ?? false;
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-xs text-muted-foreground line-clamp-1">{url}</div>
                        </td>
                        <td className="px-4 py-3">
                          <SafeImage src={url} alt="img" fallbackKey={id} className="h-12 w-24 rounded-xl border object-cover" />
                        </td>
                        <td className="px-4 py-3">
                          <span className="rounded-full border bg-background/60 px-3 py-1 text-xs">{isPrimary ? "Primary" : "—"}</span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => void setAsPrimary(id)} disabled={!id}>
                              Set primary
                            </Button>
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openEdit(img)} disabled={!id}>
                              Edit URL
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
        </CardContent>
      </Card>

      <Modal isOpen={isFormOpen} title="New product image" onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <Input value={form.url} onChange={(e) => setForm((f) => ({ ...f, url: e.target.value }))} placeholder="Image URL *" className="rounded-xl" />
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.isPrimary} onChange={(e) => setForm((f) => ({ ...f, isPrimary: e.target.checked }))} />
            Set as primary
          </label>
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={create}>
              Create
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isEditOpen} title={editId ? `Edit image #${editId}` : "Edit image"} onClose={() => setIsEditOpen(false)}>
        <div className="grid gap-3">
          <Input value={editUrl} onChange={(e) => setEditUrl(e.target.value)} placeholder="URL" className="rounded-xl" />
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsEditOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={saveUrl}>
              Save
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete image?"
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
