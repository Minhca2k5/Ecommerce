import { useEffect, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPost } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type AdminRole = Record<string, unknown>;

export default function AdminRolesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminRole[]>([]);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [name, setName] = useState("");

  const [lookupName, setLookupName] = useState("");
  const [lookupResult, setLookupResult] = useState<AdminRole | null>(null);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<AdminRole[]>(`/api/admin/roles`);
      setItems(asArray(res) as AdminRole[]);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load roles.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function openCreate() {
    setName("");
    setIsFormOpen(true);
  }

  async function create() {
    const payload = { name: name.trim() };
    if (!payload.name) {
      toast.push({ variant: "error", title: "Invalid form", message: "Role name is required." });
      return;
    }
    try {
      await adminPost(`/api/admin/roles`, payload);
      toast.push({ variant: "success", title: "Created", message: "Role created." });
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Create failed", message: getErrorMessage(e, "Failed to create role.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/roles/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Role deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete role.") });
    }
  }

  async function lookup() {
    const n = lookupName.trim();
    if (!n) return;
    try {
      const res = await adminGet<AdminRole>(`/api/admin/roles/by-name${buildQuery({ name: n })}`);
      setLookupResult(res ?? null);
      toast.push({ variant: "success", title: "Found", message: `Role resolved for "${n}".` });
    } catch (e) {
      setLookupResult(null);
      toast.push({ variant: "error", title: "Not found", message: getErrorMessage(e, "Role not found.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Roles</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
              New role
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <div className="md:col-span-2">
              <Input value={lookupName} onChange={(e) => setLookupName(e.target.value)} placeholder="Lookup by name (e.g. ADMIN)" className="rounded-xl" />
            </div>
            <Button variant="outline" className="h-10 rounded-xl" onClick={() => void lookup()}>
              Lookup
            </Button>
          </div>
          {lookupResult ? (
            <div className="rounded-xl border bg-background p-4 text-sm">
              Resolved: <span className="font-semibold">{getString(lookupResult, "name") ?? "-"}</span> (id: {getNumber(lookupResult, "id") ?? "-"})
            </div>
          ) : null}

          <div className="table-shell">
            <table className="min-w-[640px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Role</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={2}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={2}>
                      No roles found.
                    </td>
                  </tr>
                ) : (
                  items.map((r) => {
                    const id = getNumber(r, "id") ?? 0;
                    const roleName = getString(r, "name") ?? "-";
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{roleName}</div>
                          <div className="text-sm text-muted-foreground">#{id}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button
                              variant="outline"
                              className="h-9 rounded-xl action-danger"
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

      <Modal isOpen={isFormOpen} title="New role" onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <Input value={name} onChange={(e) => setName(e.target.value)} placeholder="Role name *" className="rounded-xl" />
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90" onClick={create}>
              Create
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete role?"
        description="Make sure no users depend on this role."
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
