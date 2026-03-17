import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import ConfirmDialog from "@/components/ConfirmDialog";
import { adminDelete, adminGet, adminPost } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminUser = Record<string, unknown>;

export default function AdminUsersPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminUser[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qUsername, setQUsername] = useState("");
  const [qEmail, setQEmail] = useState("");
  const [qEnabled, setQEnabled] = useState<string>("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminUser | null>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [isCartOpen, setIsCartOpen] = useState(false);
  const [cartUserId, setCartUserId] = useState<number | null>(null);
  const [cart, setCart] = useState<Record<string, unknown> | null>(null);
  const [cartItems, setCartItems] = useState<Record<string, unknown>[]>([]);
  const [isCartLoading, setIsCartLoading] = useState(false);
  const [cartError, setCartError] = useState<string | null>(null);

  const [form, setForm] = useState({
    username: "",
    email: "",
    password: "",
    fullName: "",
    phone: "",
    enabled: true,
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      username: qUsername.trim() || undefined,
      email: qEmail.trim() || undefined,
      enabled: qEnabled === "" ? undefined : qEnabled === "true",
      sort: "id,desc",
    });
  }, [page, qEmail, qEnabled, qUsername, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminUser>>(`/api/admin/users${query}`);
      setItems(asArray(res?.content) as AdminUser[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load users.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  function openCreate() {
    setForm({ username: "", email: "", password: "", fullName: "", phone: "", enabled: true });
    setIsFormOpen(true);
  }

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminUser>(`/api/admin/users/${id}/details`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load user details.") });
    }
  }

  async function openCart(userId: number) {
    if (!userId) return;
    setIsCartOpen(true);
    setCartUserId(userId);
    setCart(null);
    setCartItems([]);
    setCartError(null);
    setIsCartLoading(true);
    try {
      const res = await adminGet<Record<string, unknown>>(`/api/admin/users/${userId}/cart`);
      setCart(res ?? null);
      const embedded = (res as any)?.items ?? (res as any)?.cartItems ?? null;
      setCartItems(Array.isArray(embedded) ? embedded : []);
    } catch (e) {
      setCartError(getErrorMessage(e, "Failed to load cart."));
    } finally {
      setIsCartLoading(false);
    }
  }

  async function loadCartItemsAll() {
    const cartId = getNumber(cart, "id") ?? 0;
    if (!cartId) return;
    setIsCartLoading(true);
    setCartError(null);
    try {
      const items = await adminGet<Record<string, unknown>[]>(`/api/admin/carts/${cartId}/items/all`);
      setCartItems(Array.isArray(items) ? items : []);
    } catch (e) {
      setCartError(getErrorMessage(e, "Failed to load cart items."));
    } finally {
      setIsCartLoading(false);
    }
  }

  async function checkExists(kind: "username" | "email", value: string) {
    const v = value.trim();
    if (!v) return;
    try {
      const res = await adminGet<boolean>(`/api/admin/users/exists/${kind}${buildQuery({ [kind]: v })}`);
      toast.push({
        variant: res ? "error" : "success",
        title: res ? "Already exists" : "Available",
        message: res ? `This ${kind} is already taken.` : `This ${kind} looks available.`,
      });
    } catch (e) {
      toast.push({ variant: "error", title: "Check failed", message: getErrorMessage(e, "Failed to check availability.") });
    }
  }

  async function save() {
    const payload = {
      username: form.username.trim(),
      email: form.email.trim(),
      password: form.password,
      fullName: form.fullName.trim() || null,
      phone: form.phone.trim() || null,
      enabled: Boolean(form.enabled),
    };
    if (!payload.username || !payload.email || !payload.password) {
      toast.push({ variant: "error", title: "Invalid form", message: "Username, email and password are required." });
      return;
    }

    try {
      await adminPost(`/api/admin/users`, payload);
      toast.push({ variant: "success", title: "Created", message: "User created." });
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Create failed", message: getErrorMessage(e, "Failed to create user.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/users/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "User deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete user.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Users</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
              New user
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qUsername} onChange={(e) => setQUsername(e.target.value)} placeholder="Username" className="rounded-md" />
            <Input value={qEmail} onChange={(e) => setQEmail(e.target.value)} placeholder="Email" className="rounded-md" />
            <select title="Select option" value={qEnabled} onChange={(e) => setQEnabled(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All</option>
              <option value="true">Enabled</option>
              <option value="false">Disabled</option>
            </select>
          </div>

          <div className="table-shell">
            <table className="min-w-[760px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">User</th>
                  <th className="px-4 py-3 text-left font-medium">Email</th>
                  <th className="px-4 py-3 text-left font-medium">Enabled</th>
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
                      No user matches your current filters.
                    </td>
                  </tr>
                ) : (
                  items.map((u) => {
                    const id = getNumber(u, "id") ?? 0;
                    const username = getString(u, "username") ?? "-";
                    const email = getString(u, "email") ?? "-";
                    const enabled = getBoolean(u, "enabled");
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">{username}</div>
                          <div className="text-sm text-muted-foreground">#{id}</div>
                        </td>
                        <td className="px-4 py-3">{email}</td>
                        <td className="px-4 py-3">
                          <span className={["rounded-full border px-3 py-1 text-sm", enabled ? "bg-emerald-500/10 text-emerald-700" : "bg-rose-500/10 text-rose-700"].join(" ")}>
                            {enabled ? "Enabled" : "Disabled"}
                          </span>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => openDetails(id)} disabled={!id}>
                              Details
                            </Button>
                            <Button variant="outline" className="h-9 rounded-md" onClick={() => void openCart(id)} disabled={!id}>
                              Cart
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

      <Modal isOpen={isFormOpen} title="New user" onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <div className="grid gap-3 md:grid-cols-2">
            <div className="flex gap-2">
              <Input value={form.username} onChange={(e) => setForm((f) => ({ ...f, username: e.target.value }))} placeholder="Username *" className="rounded-md" />
              <Button variant="outline" className="h-10 rounded-md" onClick={() => void checkExists("username", form.username)}>
                Check
              </Button>
            </div>
            <div className="flex gap-2">
              <Input value={form.email} onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))} placeholder="Email *" className="rounded-md" />
              <Button variant="outline" className="h-10 rounded-md" onClick={() => void checkExists("email", form.email)}>
                Check
              </Button>
            </div>
          </div>
          <Input value={form.password} onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))} placeholder="Password *" className="rounded-md" />
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.fullName} onChange={(e) => setForm((f) => ({ ...f, fullName: e.target.value }))} placeholder="Full name" className="rounded-md" />
            <Input value={form.phone} onChange={(e) => setForm((f) => ({ ...f, phone: e.target.value }))} placeholder="Phone" className="rounded-md" />
          </div>
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.enabled} onChange={(e) => setForm((f) => ({ ...f, enabled: e.target.checked }))} />
            Enabled
          </label>
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-md" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={save}>
              Create
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isDetailsOpen} title={detailsId ? `User #${detailsId}` : "User"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">{getString(details ?? {}, "username") ?? "-"}</div>
            <div className="mt-1 text-sm text-muted-foreground">{getString(details ?? {}, "email") ?? "-"}</div>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm text-muted-foreground">Full name</div>
              <div className="mt-1 text-sm font-semibold">{getString(details ?? {}, "fullName") ?? "-"}</div>
            </div>
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm text-muted-foreground">Phone</div>
              <div className="mt-1 text-sm font-semibold">{getString(details ?? {}, "phone") ?? "-"}</div>
            </div>
          </div>
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">Roles</div>
            <div className="mt-2 flex flex-wrap gap-2">
              {asArray((details ?? {})["roles"]).length ? (
                asArray((details ?? {})["roles"]).map((r, idx) => (
                  <span key={idx} className="rounded-full border bg-background/50 px-3 py-1 text-sm">
                    {typeof r === "string" ? r : getString(r as any, "name") ?? "ROLE"}
                  </span>
                ))
              ) : (
                <div className="text-sm text-muted-foreground">No roles in response.</div>
              )}
            </div>
          </div>
          <div className="flex justify-end">
            <Button variant="outline" className="rounded-md" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isCartOpen} title={cartUserId ? `User #${cartUserId} cart` : "Cart"} onClose={() => setIsCartOpen(false)}>
        <div className="space-y-3">
          {isCartLoading ? <div className="text-sm text-muted-foreground">Loading...</div> : null}
          {cartError ? (
            <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{cartError}</div>
          ) : null}
          {cart ? (
            <div className="rounded-md border bg-background p-4">
              <div className="text-sm font-semibold">Cart #{getNumber(cart, "id") ?? "-"}</div>
              <div className="mt-1 text-sm text-muted-foreground">
                Currency: {getString(cart, "currency") ?? "-"}  -  Items: {getNumber(cart, "itemCount") ?? getNumber(cart, "itemsCount") ?? cartItems.length}
              </div>
            </div>
          ) : null}

          <div className="flex items-center justify-between gap-2">
            <div className="text-sm font-medium">Items</div>
            <Button variant="outline" className="h-9 rounded-md bg-background" disabled={isCartLoading || !cart} onClick={() => void loadCartItemsAll()}>
              Load items (all)
            </Button>
          </div>
          {cartItems.length ? (
            <div className="space-y-2">
              {cartItems.slice(0, 20).map((it, idx) => (
                <div key={String((it as any)?.id ?? idx)} className="flex items-start justify-between gap-3 rounded-md border bg-background p-3">
                  <div className="min-w-0">
                    <div className="truncate text-sm font-semibold">{getString(it, "productName") ?? "Item"}</div>
                    <div className="mt-1 text-sm text-muted-foreground">
                      productId: {getNumber(it, "productId") ?? "-"}  -  qty: {getNumber(it, "quantity") ?? "-"}
                    </div>
                  </div>
                  <div className="text-sm font-semibold">{getNumber(it, "lineTotal") ?? "-"}</div>
                </div>
              ))}
                          </div>
          ) : (
            <div className="text-sm text-muted-foreground">No cart items loaded yet. Click "Load items (all)" to fetch details.</div>
          )}

          <div className="flex justify-end">
            <Button variant="outline" className="h-10 rounded-md bg-background" onClick={() => setIsCartOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete user?"
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


