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
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminInventory = Record<string, unknown>;
type AdminProduct = Record<string, unknown>;
type AdminWarehouse = Record<string, unknown>;

export default function AdminInventoriesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminInventory[]>([]);
  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [warehouses, setWarehouses] = useState<AdminWarehouse[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qProductId, setQProductId] = useState("");
  const [qWarehouseId, setQWarehouseId] = useState("");
  const [qHasAvailable, setQHasAvailable] = useState<string>("");

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [editQtyId, setEditQtyId] = useState<number | null>(null);
  const [editQtyType, setEditQtyType] = useState<"stock" | "reserved">("stock");
  const [editQtyValue, setEditQtyValue] = useState("");
  const [isQtyOpen, setIsQtyOpen] = useState(false);

  const [form, setForm] = useState({
    productId: "",
    warehouseId: "",
    stockQty: "0",
    reservedQty: "0",
  });

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      productId: qProductId.trim() ? Number(qProductId) : undefined,
      warehouseId: qWarehouseId.trim() ? Number(qWarehouseId) : undefined,
      hasAvailableStock: qHasAvailable === "" ? undefined : qHasAvailable === "true",
      sort: "id,desc",
    });
  }, [page, qHasAvailable, qProductId, qWarehouseId, size]);

  async function load() {
    setIsLoading(true);
    try {
      const [res, productRes, warehouseRes] = await Promise.all([
        adminGet<PageResponse<AdminInventory>>(`/api/admin/inventories${query}`),
        products.length
          ? Promise.resolve({ content: products } as PageResponse<AdminProduct>)
          : adminGet<PageResponse<AdminProduct>>(`/api/admin/products${buildQuery({ page: 0, size: 200, sort: "id,desc" })}`),
        warehouses.length
          ? Promise.resolve({ content: warehouses } as PageResponse<AdminWarehouse>)
          : adminGet<PageResponse<AdminWarehouse>>(`/api/admin/warehouses${buildQuery({ page: 0, size: 200, sort: "id,asc" })}`),
      ]);
      setItems(asArray(res?.content) as AdminInventory[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
      setProducts(asArray(productRes?.content) as AdminProduct[]);
      setWarehouses(asArray(warehouseRes?.content) as AdminWarehouse[]);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load inventories.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  const productOptions = useMemo(() => {
    return products
      .map((product) => ({
        id: getNumber(product, "id") ?? 0,
        name: getString(product, "name") ?? "Product",
        slug: getString(product, "slug") ?? "",
      }))
      .filter((product) => product.id > 0);
  }, [products]);

  const warehouseOptions = useMemo(() => {
    return warehouses
      .map((warehouse) => ({
        id: getNumber(warehouse, "id") ?? 0,
        name: getString(warehouse, "name") ?? "Warehouse",
      }))
      .filter((warehouse) => warehouse.id > 0);
  }, [warehouses]);

  function openCreate() {
    setForm({ productId: "", warehouseId: "", stockQty: "0", reservedQty: "0" });
    setIsFormOpen(true);
  }

  async function create() {
    const payload = {
      productId: Number(form.productId),
      warehouseId: Number(form.warehouseId),
      stockQty: Number(form.stockQty),
      reservedQty: Number(form.reservedQty),
    };
    if (!payload.productId || !payload.warehouseId || payload.stockQty < 0 || payload.reservedQty < 0) {
      toast.push({ variant: "error", title: "Invalid form", message: "Product, warehouse, and non-negative quantities are required." });
      return;
    }
    try {
      await adminPost(`/api/admin/inventories`, payload);
      toast.push({ variant: "success", title: "Created", message: "Inventory created." });
      setIsFormOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Create failed", message: getErrorMessage(e, "Failed to create inventory.") });
    }
  }

  function openQty(inv: AdminInventory, type: "stock" | "reserved") {
    const id = getNumber(inv, "id") ?? 0;
    if (!id) return;
    setEditQtyId(id);
    setEditQtyType(type);
    setEditQtyValue(String(type === "stock" ? inv["stockQty"] ?? "" : inv["reservedQty"] ?? ""));
    setIsQtyOpen(true);
  }

  async function saveQty() {
    const id = Number(editQtyId ?? 0);
    const qty = Number(editQtyValue);
    if (!id || !Number.isFinite(qty) || qty < 0) {
      toast.push({ variant: "error", title: "Invalid quantity", message: "Quantity must be a non-negative number." });
      return;
    }
    try {
      await adminPatch(`/api/admin/inventories/${id}/${editQtyType}${buildQuery({ quantity: qty })}`);
      toast.push({ variant: "success", title: "Updated", message: "Quantity updated." });
      setIsQtyOpen(false);
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(e, "Failed to update quantity.") });
    }
  }

  async function remove(id: number) {
    try {
      await adminDelete(`/api/admin/inventories/${id}`);
      toast.push({ variant: "success", title: "Deleted", message: "Inventory deleted." });
      await load();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete inventory.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Inventories</CardTitle>
          </div>
          <div className="flex gap-2">
            <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
              Refresh
            </Button>
            <Button className="h-9 rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={openCreate}>
              New inventory
            </Button>
          </div>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <select title="Filter by product" value={qProductId} onChange={(e) => setQProductId(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All products</option>
              {productOptions.map((product) => (
                <option key={product.id} value={String(product.id)}>
                  {product.name}{product.slug ? ` (${product.slug})` : ""}
                </option>
              ))}
            </select>
            <select title="Filter by warehouse" value={qWarehouseId} onChange={(e) => setQWarehouseId(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All warehouses</option>
              {warehouseOptions.map((warehouse) => (
                <option key={warehouse.id} value={String(warehouse.id)}>
                  {warehouse.name}
                </option>
              ))}
            </select>
            <select title="Select option" value={qHasAvailable} onChange={(e) => setQHasAvailable(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All availability states</option>
              <option value="true">In stock</option>
              <option value="false">Out of stock</option>
            </select>
          </div>

          <div className="table-shell">
            <table className="min-w-[900px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Inventory</th>
                  <th className="px-4 py-3 text-left font-medium">Product</th>
                  <th className="px-4 py-3 text-left font-medium">Warehouse</th>
                  <th className="px-4 py-3 text-left font-medium">Stock</th>
                  <th className="px-4 py-3 text-left font-medium">Reserved</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={6}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={6}>
                      No inventory rows match your current filters.
                    </td>
                  </tr>
                ) : (
                  items.map((inv) => {
                    const id = getNumber(inv, "id") ?? 0;
                    const productName = getString(inv, "productName") ?? getString(inv, "productSlug") ?? "-";
                    const productSlug = getString(inv, "productSlug") ?? "";
                    const warehouseName = getString(inv, "warehouseName") ?? "-";
                    const stockQty = Number(inv["stockQty"] ?? 0);
                    const reservedQty = Number(inv["reservedQty"] ?? 0);
                    const hasAvailable = getBoolean(inv, "hasAvailableStock");
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground">{hasAvailable === undefined ? "" : hasAvailable ? "In stock" : "Out of stock"}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-medium">{productName}</div>
                          <div className="text-sm text-muted-foreground">{productSlug}</div>
                        </td>
                        <td className="px-4 py-3">{warehouseName}</td>
                        <td className="px-4 py-3">
                          <button className="rounded-md border bg-background px-3 py-1 text-sm hover:bg-muted" onClick={() => openQty(inv, "stock")}>
                            {stockQty}
                          </button>
                        </td>
                        <td className="px-4 py-3">
                          <button className="rounded-md border bg-background px-3 py-1 text-sm hover:bg-muted" onClick={() => openQty(inv, "reserved")}>
                            {reservedQty}
                          </button>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex justify-end gap-2">
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

      <Modal isOpen={isFormOpen} title="New inventory" onClose={() => setIsFormOpen(false)}>
        <div className="grid gap-3">
          <div className="grid gap-3 md:grid-cols-2">
            <select title="Product" value={form.productId} onChange={(e) => setForm((f) => ({ ...f, productId: e.target.value }))} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">Select product</option>
              {productOptions.map((product) => (
                <option key={product.id} value={String(product.id)}>
                  {product.name}{product.slug ? ` (${product.slug})` : ""}
                </option>
              ))}
            </select>
            <select title="Warehouse" value={form.warehouseId} onChange={(e) => setForm((f) => ({ ...f, warehouseId: e.target.value }))} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">Select warehouse</option>
              {warehouseOptions.map((warehouse) => (
                <option key={warehouse.id} value={String(warehouse.id)}>
                  {warehouse.name}
                </option>
              ))}
            </select>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            <Input value={form.stockQty} onChange={(e) => setForm((f) => ({ ...f, stockQty: e.target.value }))} placeholder="Stock qty" className="rounded-md" />
            <Input value={form.reservedQty} onChange={(e) => setForm((f) => ({ ...f, reservedQty: e.target.value }))} placeholder="Reserved qty" className="rounded-md" />
          </div>
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-md" onClick={() => setIsFormOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={create}>
              Create
            </Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isQtyOpen} title={editQtyId ? `Update ${editQtyType} quantity for #${editQtyId}` : "Update quantity"} onClose={() => setIsQtyOpen(false)}>
        <div className="grid gap-3">
          <Input value={editQtyValue} onChange={(e) => setEditQtyValue(e.target.value)} placeholder="Quantity" className="rounded-md" />
          <div className="mt-2 flex justify-end gap-2">
            <Button variant="outline" className="rounded-md" onClick={() => setIsQtyOpen(false)}>
              Cancel
            </Button>
            <Button className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90" onClick={saveQty}>
              Save
            </Button>
          </div>
        </div>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteId)}
        title="Delete inventory?"
        description="This removes the inventory row."
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
