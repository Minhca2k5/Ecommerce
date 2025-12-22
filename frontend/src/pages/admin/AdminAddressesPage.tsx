import { useEffect, useMemo, useState } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import Modal from "@/components/Modal";
import { adminGet } from "@/lib/adminApi";
import { buildQuery } from "@/lib/apiClient";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { asArray, getNumber, getString } from "@/lib/safe";

type PageResponse<T> = {
  content?: T[];
  totalPages?: number;
};

type AdminAddress = Record<string, unknown>;

export default function AdminAddressesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [items, setItems] = useState<AdminAddress[]>([]);
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);

  const [qUserId, setQUserId] = useState("");
  const [qCity, setQCity] = useState("");
  const [qCountry, setQCountry] = useState("");

  const [detailsId, setDetailsId] = useState<number | null>(null);
  const [details, setDetails] = useState<AdminAddress | null>(null);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);

  const [defaultUserId, setDefaultUserId] = useState("");
  const [defaultAddress, setDefaultAddress] = useState<AdminAddress | null>(null);

  const query = useMemo(() => {
    return buildQuery({
      page,
      size,
      userId: qUserId.trim() ? Number(qUserId) : undefined,
      city: qCity.trim() || undefined,
      country: qCountry.trim() || undefined,
      sort: "id,desc",
    });
  }, [page, qCity, qCountry, qUserId, size]);

  async function load() {
    setIsLoading(true);
    try {
      const res = await adminGet<PageResponse<AdminAddress>>(`/api/admin/addresses${query}`);
      setItems(asArray(res?.content) as AdminAddress[]);
      setTotalPages(Number(res?.totalPages ?? 1) || 1);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load addresses.") });
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
      const res = await adminGet<AdminAddress>(`/api/admin/addresses/${id}`);
      setDetailsId(id);
      setDetails(res ?? null);
      setIsDetailsOpen(true);
    } catch (e) {
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load address details.") });
    }
  }

  async function loadDefault() {
    const id = defaultUserId.trim() ? Number(defaultUserId) : 0;
    if (!id) return;
    try {
      const res = await adminGet<AdminAddress>(`/api/admin/addresses/user/${id}/default`);
      setDefaultAddress(res ?? null);
      toast.push({ variant: "success", title: "Loaded", message: `Default address loaded for user #${id}.` });
    } catch (e) {
      setDefaultAddress(null);
      toast.push({ variant: "error", title: "Load failed", message: getErrorMessage(e, "Failed to load default address.") });
    }
  }

  function renderLine(a: AdminAddress | null) {
    if (!a) return "-";
    const parts = [getString(a, "line1"), getString(a, "line2"), getString(a, "city"), getString(a, "state"), getString(a, "country")].filter(Boolean);
    return parts.length ? parts.join(", ") : "-";
  }

  return (
    <>
      <Card className="border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader className="flex flex-row items-start justify-between gap-3">
          <div>
            <CardTitle>Addresses</CardTitle>
            <div className="mt-1 text-sm text-muted-foreground">Search addresses, view details, and lookup user default address.</div>
          </div>
          <Button variant="outline" className="h-9 rounded-xl" onClick={load} disabled={isLoading}>
            Refresh
          </Button>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="grid gap-3 md:grid-cols-3">
            <Input value={qUserId} onChange={(e) => setQUserId(e.target.value)} placeholder="User ID" className="rounded-xl" />
            <Input value={qCity} onChange={(e) => setQCity(e.target.value)} placeholder="City" className="rounded-xl" />
            <Input value={qCountry} onChange={(e) => setQCountry(e.target.value)} placeholder="Country" className="rounded-xl" />
          </div>

          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">Default address lookup</div>
            <div className="mt-3 grid gap-2 md:grid-cols-3">
              <Input value={defaultUserId} onChange={(e) => setDefaultUserId(e.target.value)} placeholder="User ID" className="rounded-xl" />
              <Button variant="outline" className="h-10 rounded-xl md:col-span-1" onClick={() => void loadDefault()}>
                Load default
              </Button>
              <div className="md:col-span-1 text-sm text-muted-foreground">{defaultAddress ? renderLine(defaultAddress) : "—"}</div>
            </div>
          </div>

          <div className="overflow-x-auto rounded-2xl border bg-background/70">
            <table className="min-w-[760px] w-full text-sm">
              <thead className="bg-muted/50 text-xs text-muted-foreground">
                <tr>
                  <th className="px-4 py-3 text-left font-medium">Address</th>
                  <th className="px-4 py-3 text-left font-medium">User</th>
                  <th className="px-4 py-3 text-right font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {isLoading ? (
                  Array.from({ length: 6 }).map((_, i) => (
                    <tr key={i} className="border-t">
                      <td className="px-4 py-3" colSpan={3}>
                        <div className="h-4 w-full animate-pulse rounded bg-muted" />
                      </td>
                    </tr>
                  ))
                ) : !items.length ? (
                  <tr className="border-t">
                    <td className="px-4 py-6 text-center text-muted-foreground" colSpan={3}>
                      No addresses found.
                    </td>
                  </tr>
                ) : (
                  items.map((a) => {
                    const id = getNumber(a, "id") ?? 0;
                    const userId = getNumber(a, "userId");
                    return (
                      <tr key={String(id)} className="border-t">
                        <td className="px-4 py-3">
                          <div className="font-medium">#{id}</div>
                          <div className="text-xs text-muted-foreground">{renderLine(a)}</div>
                        </td>
                        <td className="px-4 py-3">{userId ?? "-"}</td>
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

      <Modal isOpen={isDetailsOpen} title={detailsId ? `Address #${detailsId}` : "Address"} onClose={() => setIsDetailsOpen(false)}>
        <div className="space-y-3">
          <div className="rounded-2xl border bg-background/60 p-4">
            <div className="text-sm font-semibold">{renderLine(details)}</div>
            <div className="mt-1 text-xs text-muted-foreground">User: {getNumber(details ?? {}, "userId") ?? "-"}</div>
          </div>
          <div className="grid gap-3 md:grid-cols-2">
            {[
              ["City", getString(details ?? {}, "city")],
              ["State", getString(details ?? {}, "state")],
              ["Country", getString(details ?? {}, "country")],
              ["Zipcode", getString(details ?? {}, "zipcode")],
            ].map(([label, value]) => (
              <div key={label} className="rounded-2xl border bg-background/60 p-4">
                <div className="text-xs text-muted-foreground">{label}</div>
                <div className="mt-1 text-sm font-semibold">{value || "-"}</div>
              </div>
            ))}
          </div>
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
