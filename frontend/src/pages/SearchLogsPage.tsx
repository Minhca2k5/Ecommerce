import { useEffect, useMemo, useState } from "react";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { deleteAllSearchLogs, deleteSearchLog, listMySearchLogs, type SearchLogResponse } from "@/lib/searchLogApi";

function formatTime(iso?: string) {
  if (!iso) return "-";
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
}

export default function SearchLogsPage() {
  const toast = useToast();
  const [items, setItems] = useState<SearchLogResponse[]>([]);
  const [filter, setFilter] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [deleteTarget, setDeleteTarget] = useState<SearchLogResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [isClearOpen, setIsClearOpen] = useState(false);
  const [isClearing, setIsClearing] = useState(false);

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const data = await listMySearchLogs();
      setItems(data ?? []);
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load search logs."));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void refresh();
  }, []);

  const filtered = useMemo(() => {
    const q = filter.trim().toLowerCase();
    if (!q) return items;
    return items.filter((i) => String(i.keyword || "").toLowerCase().includes(q));
  }, [filter, items]);

  async function onRemove() {
    const id = Number(deleteTarget?.id ?? 0);
    if (!id) return;
    setIsDeleting(true);
    try {
      await deleteSearchLog(id);
      toast.push({ variant: "success", title: "Deleted", message: "Search log removed." });
      setDeleteTarget(null);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(e, "Failed to delete search log.") });
    } finally {
      setIsDeleting(false);
    }
  }

  async function onClear() {
    setIsClearing(true);
    try {
      await deleteAllSearchLogs();
      toast.push({ variant: "success", title: "Cleared", message: "Search logs cleared." });
      setIsClearOpen(false);
      await refresh();
    } catch (e) {
      toast.push({ variant: "error", title: "Clear failed", message: getErrorMessage(e, "Failed to clear search logs.") });
    } finally {
      setIsClearing(false);
    }
  }

  if (isLoading) return <div className="space-y-4"><LoadingCard /><LoadingCard /></div>;
  if (error) return <EmptyState title="Couldn't load search logs" description={error} action={<Button onClick={refresh} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">Retry</Button>} />;
  if (!items.length) return <EmptyState title="No search logs" description="Search products to build your history." />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Account</div>
          <div className="text-3xl font-semibold tracking-tight">Search logs</div>
          <div className="mt-1 text-sm text-muted-foreground">Your recent search keywords.</div>
        </div>
        <Button variant="outline" className="rounded-xl border-rose-500/20 text-rose-700 hover:bg-rose-500/10" onClick={() => setIsClearOpen(true)}>
          Clear all
        </Button>
      </div>

      <Card className="shine">
        <CardHeader>
          <CardTitle className="text-base">Filter</CardTitle>
        </CardHeader>
        <CardContent>
          <Input value={filter} onChange={(e) => setFilter(e.target.value)} placeholder="Filter by keyword..." className="rounded-xl" />
        </CardContent>
      </Card>

      <div className="grid gap-3 lg:grid-cols-2">
        {filtered.map((i) => (
          <Card key={String(i.id)} className="pressable">
            <CardHeader className="flex flex-row items-start justify-between gap-3">
              <div>
                <CardTitle className="text-base">{i.keyword || "-"}</CardTitle>
                <div className="mt-1 text-xs text-muted-foreground">{formatTime(i.createdAt)}</div>
              </div>
              <Button variant="outline" className="rounded-xl border-rose-500/20 text-rose-700 hover:bg-rose-500/10" onClick={() => setDeleteTarget(i)}>
                Delete
              </Button>
            </CardHeader>
          </Card>
        ))}
      </div>

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Delete this log?"
        description="This search keyword will be removed from your history."
        confirmText="Delete"
        variant="danger"
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={onRemove}
      />
      <ConfirmDialog
        isOpen={isClearOpen}
        title="Clear search logs?"
        description="This will remove all your search history."
        confirmText="Clear"
        variant="danger"
        isLoading={isClearing}
        onClose={() => setIsClearOpen(false)}
        onConfirm={onClear}
      />
    </div>
  );
}

