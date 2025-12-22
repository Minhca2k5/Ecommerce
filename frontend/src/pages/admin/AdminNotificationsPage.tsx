import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import { adminGet, adminPost, adminPut } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminNotification = Record<string, unknown>;

export default function AdminNotificationsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminNotification[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qIsRead, setQIsRead] = useState<string>("");
  const [qIsHidden, setQIsHidden] = useState<string>("");
  const [qReferenceType, setQReferenceType] = useState("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [form, setForm] = useState({
    userId: "",
    title: "",
    message: "",
    type: "",
    referenceId: "",
    referenceType: "",
    isRead: false,
    isHidden: false,
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      isRead: qIsRead === "" ? undefined : qIsRead === "true",
      isHidden: qIsHidden === "" ? undefined : qIsHidden === "true",
      referenceType: qReferenceType.trim() || undefined,
      sort: "id,desc",
    });
  }, [page, qIsHidden, qIsRead, qReferenceType, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminNotification>>(`/api/admin/notifications/filter${query}`);
      setItems(asArray(res?.content) as AdminNotification[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load notifications.") });
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
    setForm({ userId: "", title: "", message: "", type: "", referenceId: "", referenceType: "", isRead: false, isHidden: false });
    setIsFormOpen(true);
  }

  function openEdit(n: AdminNotification) {
    const id = getNumber(n, "id") ?? 0;
    if (!id) return;
    setEditingId(id);
    setForm({
      userId: String(getNumber(n, "userId") ?? ""),
      title: getString(n, "title") ?? "",
      message: getString(n, "message") ?? "",
      type: getString(n, "type") ?? "",
      referenceId: String(getNumber(n, "referenceId") ?? ""),
      referenceType: getString(n, "referenceType") ?? "",
      isRead: getBoolean(n, "isRead") ?? false,
      isHidden: getBoolean(n, "isHidden") ?? false,
    });
    setIsFormOpen(true);
  }

  async function save() {
    if (!editingId) {
      const payload = {
        userId: Number(form.userId),
        title: form.title.trim(),
        message: form.message.trim(),
        type: form.type.trim(),
        referenceId: form.referenceId.trim() ? Number(form.referenceId) : null,
        referenceType: form.referenceType.trim() || null,
      };
      if (!payload.userId || !payload.title || !payload.message || !payload.type) {
        toast.push({ variant: "error", title: "Invalid form", message: "userId/title/message/type are required for create." });
        return;
      }
      try {
        await adminPost(`/api/admin/notifications`, payload);
        toast.push({ variant: "success", title: "Created", message: "Notification created." });
        setIsFormOpen(false);
        await load();
      } catch (e) {
        toast.push({ variant: "error", title: "Create failed", message: getErrorMessage(e, "Failed to create notification.") });
      }
      return;
    }

    const payload = {
      title: form.title.trim() || null,
      message: form.message.trim() || null,
      type: form.type.trim() || null,
      referenceId: form.referenceId.trim() ? Number(form.referenceId) : null,
      referenceType: form.referenceType.trim() || null,
      isRead: Boolean(form.isRead),
      isHidden: Boolean(form.isHidden),
    };

    try {
      await adminPut(`/api/admin/notifications/${editingId}`, payload);
      toast.push({ variant: "success", title: "Updated", message: "Notification updated." });
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update notification.") });
    }
  }

  return (
    <>
      <Card className="border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Notifications</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">Filter, create, and update notifications.</div>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95" onClick={openCreate}>
              New notification
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <select value={qIsRead} onChange={(e) => setQIsRead(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All read states</option>
              <option value="true">Read</option>
              <option value="false">Unread</option>
            </select>
            <select value={qIsHidden} onChange={(e) => setQIsHidden(e.target.value)} className="h-10 rounded-xl border bg-background px-3 text-sm">
              <option value="">All visibility</option>
              <option value="true">Hidden</option>
              <option value="false">Visible</option>
            </select>
            <Input value={qReferenceType} onChange={(e) => setQReferenceType(e.target.value)} placeholder="Reference type" className="rounded-xl" />
          </div>

          <div className="overflow-x-auto rounded-2xl border bg-background/70">
            <table className="min-w-[900px] w-full text-sm">
              <thead className="bg-muted/50 text-xs text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Notification</th>
                  <th className="px-4 py-3 text-left font-medium">User</th>
                  <th className="px-4 py-3 text-left font-medium">Flags</th>
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
                      No notifications found.
                    </td>
                  </tr>
                ) : (
                  items.map((n) => {
                    const id = getNumber(n, "id") ?? 0;
                    const isRead = getBoolean(n, "isRead") ?? false;
                    const isHidden = getBoolean(n, "isHidden") ?? false;
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{getString(n, "title") ?? "-"}</div>
                          <div className="text-xs text-muted-foreground line-clamp-1">{getString(n, "message") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">{getNumber(n, "userId") ?? "-"}</td>
                        <td className="px-4 py-3">
                          <span className="rounded-full border bg-background/60 px-3 py-1 text-xs">{isRead ? "Read" : "Unread"}</span>
                          <span className="ml-2 rounded-full border bg-background/60 px-3 py-1 text-xs">{isHidden ? "Hidden" : "Visible"}</span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-xl" onClick={() => openEdit(n)} disabled={!id}>
                              Edit
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

      <Modal isOpen={isFormOpen} title={editingId ? `Edit notification #${editingId}` : "New notification"} onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          {!editingId ? <Input value={form.userId} onChange={(e) => setForm((f) => ({ ...f, userId: e.target.value }))} placeholder="User ID *" className="rounded-xl" /> : null}
          <Input value={form.title} onChange={(e) => setForm((f) => ({ ...f, title: e.target.value }))} placeholder="Title *" className="rounded-xl" />
          <textarea
            value={form.message}
            onChange={(e) => setForm((f) => ({ ...f, message: e.target.value }))}
            placeholder="Message *"
            className="min-h-24 w-full rounded-xl border bg-background px-3 py-2 text-sm"
          />
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.type} onChange={(e) => setForm((f) => ({ ...f, type: e.target.value }))} placeholder="Type *" className="rounded-xl" />
            <Input value={form.referenceType} onChange={(e) => setForm((f) => ({ ...f, referenceType: e.target.value }))} placeholder="Reference type" className="rounded-xl" />
          </div>
          <Input value={form.referenceId} onChange={(e) => setForm((f) => ({ ...f, referenceId: e.target.value }))} placeholder="Reference ID" className="rounded-xl" />
          {editingId ? (
            <div className="flex flex-wrap gap-6 text-sm">
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={form.isRead} onChange={(e) => setForm((f) => ({ ...f, isRead: e.target.checked }))} />
                Read
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={form.isHidden} onChange={(e) => setForm((f) => ({ ...f, isHidden: e.target.checked }))} />
                Hidden
              </label>
            </div>
          ) : null}
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
    </>
  );
}
