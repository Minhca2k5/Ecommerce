import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import RatingStars from "@/components/RatingStars";
import { adminGet } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminReview = Record<string, unknown>;
type AdminProduct = Record<string, unknown>;
type AdminUser = Record<string, unknown>;

export default function AdminReviewsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminReview[]>([]);
  const [products, setProducts] = useState<AdminProduct[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qProductId, setQProductId] = useState("");
  const [qUserId, setQUserId] = useState("");
  const [qMinRating, setQMinRating] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminReview | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const productOptions = useMemo(
    () =>
      products
        .map((product) => ({
          id: getNumber(product, "id") ?? 0,
          name: getString(product, "name") ?? "Product",
          slug: getString(product, "slug") ?? "",
        }))
        .filter((product) => product.id > 0),
    [products],
  );

  const userOptions = useMemo(
    () =>
      users
        .map((user) => ({
          id: getNumber(user, "id") ?? 0,
          username: getString(user, "username") ?? "",
          fullName: getString(user, "fullName") ?? "",
        }))
        .filter((user) => user.id > 0),
    [users],
  );

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      productId: qProductId.trim() ? Number(qProductId) : undefined,
      userId: qUserId.trim() ? Number(qUserId) : undefined,
      minRating: qMinRating.trim() ? Number(qMinRating) : undefined,
      sort: "id,desc",
    });
  }, [page, qMinRating, qProductId, qUserId, size]);

  async function load() {
    setIsLoading(true);
    try {
      const [res, productRes, userRes] = await Promise.all([
        adminGet<PageResponse<AdminReview>>(`/api/admin/reviews${query}`),
        products.length
          ? Promise.resolve({ content: products } as PageResponse<AdminProduct>)
          : adminGet<PageResponse<AdminProduct>>(`/api/admin/products${buildQuery({ page: 0, size: 200, sort: "id,desc" })}`),
        users.length
          ? Promise.resolve({ content: users } as PageResponse<AdminUser>)
          : adminGet<PageResponse<AdminUser>>(`/api/admin/users${buildQuery({ page: 0, size: 200, sort: "id,desc" })}`),
      ]);
      setItems(asArray(res?.content) as AdminReview[]);
      setProducts(asArray(productRes?.content) as AdminProduct[]);
      setUsers(asArray(userRes?.content) as AdminUser[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load reviews.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  async function openDetails(id: number) {
    try {
      const res = await adminGet<AdminReview>(`/api/admin/reviews/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load review details.") });
    }
  }

  return (
    <>
      <Card className="border bg-background shadow-sm">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Reviews</CardTitle>
          </div>
          <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
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
            <select title="Filter by user" value={qUserId} onChange={(e) => setQUserId(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
              <option value="">All users</option>
              {userOptions.map((user) => (
                <option key={user.id} value={String(user.id)}>
                  {user.fullName || user.username}
                  {user.fullName && user.username ? ` (${user.username})` : ""}
                </option>
              ))}
            </select>
            <Input value={qMinRating} onChange={(e) => setQMinRating(e.target.value)} placeholder="Minimum rating" className="rounded-md" />
          </div>

          <div className="table-shell">
            <table className="min-w-[820px] w-full text-sm">
              <thead className="bg-muted/50 text-sm text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Review</th>
                  <th className="px-4 py-3 text-left font-medium">Product</th>
                  <th className="px-4 py-3 text-left font-medium">User</th>
                  <th className="px-4 py-3 text-left font-medium">Rating</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={5}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={5}>
                      No reviews match your current filters.
                    </td>
                  </tr>
                ) : (
                  items.map((r) => {
                    const id = getNumber(r, "id") ?? 0;
                    const rating = Number(r["rating"] ?? 0);
                    const productName = getString(r, "productName") ?? getString(r, "productSlug") ?? "-";
                    const productSlug = getString(r, "productSlug") ?? "";
                    const userLabel = getString(r, "fullName") ?? getString(r, "username") ?? "-";
                    const username = getString(r, "username") ?? "";
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground line-clamp-1">{getString(r, "comment") ?? getString(r, "commentSnapshot") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-medium">{productName}</div>
                          <div className="text-sm text-muted-foreground">{productSlug || "-"}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="font-medium">{userLabel}</div>
                          <div className="text-sm text-muted-foreground">{username || "-"}</div>
                        </td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <RatingStars rating={rating} />
                            <span className="text-sm text-muted-foreground">{rating}</span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <Button variant="outline" className="h-9 rounded-md" onClick={() => openDetails(id)} disabled={!id}>
                            Details
                          </Button>
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

      <Modal isOpen={isDetailsOpen} title={getString(details ?? {}, "productName") ?? getString(details ?? {}, "productSlug") ?? (detailsId ? "Review details" : "Review")} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm font-semibold">
              Product: {getString(details ?? {}, "productName") ?? getString(details ?? {}, "productSlug") ?? "-"} · User: {getString(details ?? {}, "fullName") ?? getString(details ?? {}, "username") ?? "-"}
            </div>
            <div className="mt-2 flex items-center gap-2">
              <RatingStars rating={Number((details ?? {})["rating"] ?? 0)} />
              <span className="text-sm text-muted-foreground">{String((details ?? {})["rating"] ?? 0)}</span>
            </div>
          </div>
          <div className="rounded-md border bg-background p-4 text-sm text-muted-foreground whitespace-pre-wrap">{getString(details ?? {}, "comment") ?? "-"}</div>
          <div className="flex justify-end">
            <Button variant="outline" className="rounded-md" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}

