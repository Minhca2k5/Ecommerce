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

export default function AdminReviewsPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminReview[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qProductId, setQProductId] = useState("");
  const [qUserId, setQUserId] = useState("");
  const [qMinRating, setQMinRating] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminReview | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

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
      const res = await adminGet<PageResponse<AdminReview>>(`/api/admin/reviews${query}`);
      setItems(asArray(res?.content) as AdminReview[]);
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
          <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qProductId} onChange={(e) => setQProductId(e.target.value)} placeholder="Product ID" className="rounded-xl" />
            <Input value={qUserId} onChange={(e) => setQUserId(e.target.value)} placeholder="User ID" className="rounded-xl" />
            <Input value={qMinRating} onChange={(e) => setQMinRating(e.target.value)} placeholder="Min rating" className="rounded-xl" />
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
                      No reviews found.
                    </td>
                  </tr>
                ) : (
                  items.map((r) => {
                    const id = getNumber(r, "id") ?? 0;
                    const rating = Number(r["rating"] ?? 0);
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-sm text-muted-foreground line-clamp-1">{getString(r, "comment") ?? getString(r, "commentSnapshot") ?? ""}</div>
                        </td>
                        <td className="px-4 py-3">{getNumber(r, "productId") ?? "-"}</td>
                        <td className="px-4 py-3">{getNumber(r, "userId") ?? "-"}</td>
                        <td className="px-4 py-3">
                          <div className="flex items-center gap-2">
                            <RatingStars rating={rating} />
                            <span className="text-sm text-muted-foreground">{rating}</span>
                          </div>
                        </td>
                        <td className="px-4 py-3 text-right">
                          <Button variant="outline" className="h-9 rounded-xl" onClick={() => openDetails(id)} disabled={!id}>
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

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Review #${detailsId}` : "Review"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-xl border bg-background p-4">
            <div className="text-sm font-semibold">
              Product: {getNumber(details ?? {}, "productId") ?? "-"} • User: {getNumber(details ?? {}, "userId") ?? "-"}
            </div>
            <div className="mt-2 flex items-center gap-2">
              <RatingStars rating={Number((details ?? {})["rating"] ?? 0)} />
              <span className="text-sm text-muted-foreground">{String((details ?? {})["rating"] ?? 0)}</span>
            </div>
          </div>
          <div className="rounded-xl border bg-background p-4 text-sm text-muted-foreground whitespace-pre-wrap">{getString(details ?? {}, "comment") ?? "-"}</div>
          <div className="flex justify-end">
            <Button variant="outline" className="rounded-xl" onClick={() => setIsDetailsOpen(false)}>
              Close
            </Button>
          </div>
        </div>
      </Modal>
    </>
  );
}
