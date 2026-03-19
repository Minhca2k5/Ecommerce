import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { adminGet, type PageResponse } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { getErrorMessage } from "@/lib/errors";
import { useToast } from "@/app/ToastProvider";
import { asArray, getBoolean, getNumber, getString } from "@/lib/safe";

type AuditLog = Record<string, unknown>;
type AdminUser = Record<string, unknown>;

export default function AdminAuditLogsPage() {
  const toast = useToast();
  const [items, setItems] = useState<AuditLog[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [totalPages, setTotalPages] = useState(1);

  const [qUserId, setQUserId] = useState("");
  const [qAction, setQAction] = useState("");
  const [qEntityType, setQEntityType] = useState("");
  const [qSuccess, setQSuccess] = useState("");
  const [qFrom, setQFrom] = useState("");
  const [qTo, setQTo] = useState("");

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

  const query = useMemo(
    () =>
      buildQuery({
        page,
        size,
        sort: "id,desc",
        userId: qUserId.trim() ? Number(qUserId) : undefined,
        action: qAction.trim() || undefined,
        entityType: qEntityType.trim() || undefined,
        success: qSuccess === "" ? undefined : qSuccess === "true",
        from: qFrom || undefined,
        to: qTo || undefined,
      }),
    [page, qAction, qEntityType, qFrom, qSuccess, qTo, qUserId, size],
  );

  async function load() {
    setIsLoading(true);
    try {
      const [res, userRes] = await Promise.all([
        adminGet<PageResponse<AuditLog>>(`/api/admin/audit-logs${query}`),
        users.length
          ? Promise.resolve({ content: users } as PageResponse<AdminUser>)
          : adminGet<PageResponse<AdminUser>>(`/api/admin/users${buildQuery({ page: 0, size: 200, sort: "id,desc" })}`),
      ]);
      setItems(asArray(res?.content) as AuditLog[]);
      setUsers(asArray(userRes?.content) as AdminUser[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load audit logs.") });
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    void load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query]);

  return (
    <Card className="border bg-background shadow-sm">
      <CardHeader className="flex flex-row items-start justify-between gap-3">
        <div>
          <CardTitle>Audit logs</CardTitle>
        </div>
        <Button variant="outline" className="h-9 rounded-md" onClick={load} disabled={isLoading}>
          Refresh
        </Button>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid gap-3 md:grid-cols-3">
          <select title="Filter by user" value={qUserId} onChange={(e) => setQUserId(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
            <option value="">All users</option>
            {userOptions.map((user) => (
              <option key={user.id} value={String(user.id)}>
                {user.fullName || user.username}
                {user.fullName && user.username ? ` (${user.username})` : ""}
              </option>
            ))}
          </select>
          <Input value={qAction} onChange={(e) => setQAction(e.target.value)} placeholder="Action (e.g. ORDER_CREATED)" className="rounded-md" />
          <Input value={qEntityType} onChange={(e) => setQEntityType(e.target.value)} placeholder="Entity type (e.g. ORDER)" className="rounded-md" />
          <select title="Select option" value={qSuccess} onChange={(e) => setQSuccess(e.target.value)} className="h-10 rounded-md border bg-background px-3 text-sm">
            <option value="">All results</option>
            <option value="true">Success</option>
            <option value="false">Failed</option>
          </select>
          <Input value={qFrom} onChange={(e) => setQFrom(e.target.value)} placeholder="From (ISO datetime)" className="rounded-md" />
          <Input value={qTo} onChange={(e) => setQTo(e.target.value)} placeholder="To (ISO datetime)" className="rounded-md" />
        </div>

        <div className="table-shell">
          <table className="min-w-[1100px] w-full text-sm">
            <thead className="bg-muted/50 text-sm text-muted-foreground">
              <tr>
                <th className="px-4 py-3 text-left font-medium">Event</th>
                <th className="px-4 py-3 text-left font-medium">Actor</th>
                <th className="px-4 py-3 text-left font-medium">Target</th>
                <th className="px-4 py-3 text-left font-medium">Result</th>
                <th className="px-4 py-3 text-left font-medium">Context</th>
                <th className="px-4 py-3 text-left font-medium">Time</th>
              </tr>
            </thead>
            <tbody>
              {isLoading ? (
                Array.from({ length: 8 }).map((_, i) => (
                  <tr key={i} className="border-t">
                    <td className="px-4 py-3" colSpan={6}>
                      <div className="h-4 w-full animate-pulse rounded bg-muted" />
                    </td>
                  </tr>
                ))
              ) : !items.length ? (
                <tr className="border-t">
                  <td className="px-4 py-6 text-center text-muted-foreground" colSpan={6}>
                    No activity logs match your current filters.
                  </td>
                </tr>
              ) : (
                items.map((it) => {
                  const id = getNumber(it, "id");
                  const success = getBoolean(it, "success");
                  return (
                    <tr key={String(id ?? Math.random())} className="border-t">
                      <td className="px-4 py-3">
                        <div className="font-medium">{getString(it, "action") || "-"}</div>
                        <div className="text-sm text-muted-foreground">#{id ?? "-"}</div>
                      </td>
                      <td className="px-4 py-3">
                        <div className="font-medium">
                          {(() => {
                            const user = userOptions.find((item) => item.id === Number(getNumber(it, "userId") ?? 0));
                            return user ? user.fullName || user.username || "-" : "-";
                          })()}
                        </div>
                        <div className="text-sm text-muted-foreground">
                          {(() => {
                            const user = userOptions.find((item) => item.id === Number(getNumber(it, "userId") ?? 0));
                            return user?.username || "-";
                          })()}
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        {getString(it, "entityType") || "-"} / {getNumber(it, "entityId") ?? "-"}
                      </td>
                      <td className="px-4 py-3">
                        <span
                          className={[
                            "rounded-full border px-3 py-1 text-sm",
                            success ? "bg-emerald-500/10 text-emerald-700 border-emerald-500/20" : "bg-rose-500/10 text-rose-700 border-rose-500/20",
                          ].join(" ")}
                        >
                          {success ? "Success" : "Failed"}
                        </span>
                        {!success && getString(it, "errorMessage") ? (
                          <div className="mt-1 max-w-[260px] truncate text-sm text-rose-700">{getString(it, "errorMessage")}</div>
                        ) : null}
                      </td>
                      <td className="px-4 py-3 text-sm text-muted-foreground">
                        <div className="max-w-[320px] truncate">IP: {getString(it, "ipAddress") || "-"}</div>
                        <div className="max-w-[320px] truncate">UA: {getString(it, "userAgent") || "-"}</div>
                      </td>
                      <td className="px-4 py-3 text-sm text-muted-foreground">{getString(it, "createdAt") || "-"}</td>
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
  );
}

