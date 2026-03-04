import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useToast } from "@/app/ToastProvider";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import Modal from "@/components/Modal";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import {
  createAddress,
  deleteAddress,
  listAddresses,
  setDefaultAddress,
  updateAddress,
  type AddressResponse,
} from "@/lib/userApi";

function formatAddress(a: AddressResponse) {
  const parts = [a.line1, a.line2, a.city, a.state, a.country, a.zipcode].filter(Boolean);
  return parts.join(", ");
}

function AddressBadge({ isDefault }: { isDefault?: boolean }) {
  if (!isDefault) return null;
  return (
    <span className="inline-flex items-center gap-1 rounded-full bg-emerald-500/10 px-2 py-1 text-xs text-emerald-700 ring-1 ring-emerald-500/20">
      <span className="h-1.5 w-1.5 rounded-full bg-emerald-500" />
      Default
    </span>
  );
}

type AddressDraft = {
  line1: string;
  line2: string;
  city: string;
  state: string;
  country: string;
  zipcode: string;
  isDefault: boolean;
};

const emptyDraft: AddressDraft = {
  line1: "",
  line2: "",
  city: "",
  state: "",
  country: "",
  zipcode: "",
  isDefault: false,
};

export default function AddressesPage() {
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [addresses, setAddresses] = useState<AddressResponse[]>([]);

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editing, setEditing] = useState<AddressResponse | null>(null);
  const [draft, setDraft] = useState<AddressDraft>(emptyDraft);
  const [isSaving, setIsSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<AddressResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const sorted = useMemo(() => {
    const next = [...addresses];
    next.sort((a, b) => Number(Boolean(b.isDefault)) - Number(Boolean(a.isDefault)));
    return next;
  }, [addresses]);

  function openCreate() {
    setEditing(null);
    setDraft(emptyDraft);
    setIsModalOpen(true);
  }

  function openEdit(item: AddressResponse) {
    setEditing(item);
    setDraft({
      line1: item.line1 ?? "",
      line2: item.line2 ?? "",
      city: item.city ?? "",
      state: item.state ?? "",
      country: item.country ?? "",
      zipcode: item.zipcode ?? "",
      isDefault: Boolean(item.isDefault),
    });
    setIsModalOpen(true);
  }

  async function refresh() {
    setIsLoading(true);
    setError(null);
    try {
      const data = await listAddresses();
      setAddresses(data);
    } catch (e) {
      setError(getErrorMessage(e, "Failed to load addresses."));
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    refresh();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  async function onSave(e: React.FormEvent) {
    e.preventDefault();
    setIsSaving(true);
    try {
      if (editing?.id) {
        await updateAddress(editing.id, {
          line1: draft.line1.trim(),
          line2: draft.line2.trim() || undefined,
          city: draft.city.trim(),
          state: draft.state.trim() || undefined,
          country: draft.country.trim(),
          zipcode: draft.zipcode.trim() || undefined,
        });
        toast.push({ variant: "success", title: "Saved", message: "Address updated." });
      } else {
        const created = await createAddress({
          line1: draft.line1.trim(),
          line2: draft.line2.trim() || undefined,
          city: draft.city.trim(),
          state: draft.state.trim() || undefined,
          country: draft.country.trim(),
          zipcode: draft.zipcode.trim() || undefined,
          isDefault: draft.isDefault,
        });
        toast.push({ variant: "success", title: "Added", message: "New address created." });
        if (draft.isDefault && created.id) {
          await setDefaultAddress(created.id);
        }
      }

      setIsModalOpen(false);
      setEditing(null);
      setDraft(emptyDraft);
      await refresh();
    } catch (err) {
      toast.push({ variant: "error", title: "Save failed", message: getErrorMessage(err, "Failed to save address.") });
    } finally {
      setIsSaving(false);
    }
  }

  async function onSetDefault(addressId: number) {
    try {
      await setDefaultAddress(addressId);
      toast.push({ variant: "success", title: "Updated", message: "Default address updated." });
      await refresh();
    } catch (err) {
      toast.push({ variant: "error", title: "Update failed", message: getErrorMessage(err, "Failed to set default address.") });
    }
  }

  async function onDelete() {
    if (!deleteTarget?.id) return;
    setIsDeleting(true);
    try {
      await deleteAddress(deleteTarget.id);
      toast.push({ variant: "success", title: "Deleted", message: "Address removed." });
      setDeleteTarget(null);
      await refresh();
    } catch (err) {
      toast.push({ variant: "error", title: "Delete failed", message: getErrorMessage(err, "Failed to delete address.") });
    } finally {
      setIsDeleting(false);
    }
  }

  if (isLoading) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (error) {
    return (
      <EmptyState
        title="Couldn’t load addresses"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="h-10 rounded-xl bg-primary text-primary-foreground">
            Retry
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div className="text-2xl font-semibold">Address book</div>
          <div className="flex flex-wrap items-center gap-2">
            <Button asChild variant="outline" className="h-10 rounded-xl bg-background">
              <Link to="/me">Back</Link>
            </Button>
            <Button onClick={openCreate} className="h-10 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90">
              Add address
            </Button>
          </div>
        </div>
      </section>

      {sorted.length === 0 ? (
        <EmptyState
          title="No addresses yet"
          description="Add your first address to speed up checkout."
          action={
            <Button onClick={openCreate} className="h-10 rounded-xl bg-primary text-primary-foreground">
              Add address
            </Button>
          }
        />
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {sorted.map((a) => (
            <Card key={a.id} className="pressable overflow-hidden bg-background shadow-sm transition hover:shadow-md">
              <CardHeader className="relative flex flex-row items-start justify-between gap-3">
                <div className="min-w-0">
                  <CardTitle className="truncate">{a.city || "Address"}</CardTitle>
                  <div className="mt-1 text-sm text-muted-foreground line-clamp-2">{formatAddress(a)}</div>
                </div>
                <AddressBadge isDefault={a.isDefault} />
              </CardHeader>
              <CardContent className="relative flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex flex-wrap gap-2">
                  <Button variant="outline" className="rounded-xl bg-background" onClick={() => openEdit(a)}>
                    Edit
                  </Button>
                  <Button
                    variant="outline"
                    className="rounded-xl border-rose-500/20 bg-background text-rose-700 hover:bg-rose-500/10"
                    onClick={() => setDeleteTarget(a)}
                  >
                    Delete
                  </Button>
                </div>
                <Button
                  disabled={Boolean(a.isDefault) || !a.id}
                  onClick={() => a.id && onSetDefault(a.id)}
                  className="h-10 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90 disabled:opacity-60"
                >
                  {a.isDefault ? "Default" : "Set default"}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Modal
        isOpen={isModalOpen}
        title={editing ? "Edit address" : "Add address"}
        onClose={() => {
          if (isSaving) return;
          setIsModalOpen(false);
        }}
      >
        <form onSubmit={onSave} className="space-y-4">
          <div className="grid gap-3 sm:grid-cols-2">
            <div className="space-y-2 sm:col-span-2">
              <label className="text-sm font-medium">Address line 1</label>
              <Input className="rounded-xl" value={draft.line1} onChange={(e) => setDraft((d) => ({ ...d, line1: e.target.value }))} />
            </div>
            <div className="space-y-2 sm:col-span-2">
              <label className="text-sm font-medium">Address line 2 (optional)</label>
              <Input className="rounded-xl" value={draft.line2} onChange={(e) => setDraft((d) => ({ ...d, line2: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">City</label>
              <Input className="rounded-xl" value={draft.city} onChange={(e) => setDraft((d) => ({ ...d, city: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">State (optional)</label>
              <Input className="rounded-xl" value={draft.state} onChange={(e) => setDraft((d) => ({ ...d, state: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Country</label>
              <Input className="rounded-xl" value={draft.country} onChange={(e) => setDraft((d) => ({ ...d, country: e.target.value }))} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Zip code (optional)</label>
              <Input className="rounded-xl" value={draft.zipcode} onChange={(e) => setDraft((d) => ({ ...d, zipcode: e.target.value }))} />
            </div>
          </div>

          {!editing ? (
            <button
              type="button"
              onClick={() => setDraft((d) => ({ ...d, isDefault: !d.isDefault }))}
              className={[
                "w-full rounded-xl border px-3 py-2 text-left text-sm",
                draft.isDefault ? "border-emerald-500/30 bg-emerald-500/10 text-emerald-800" : "bg-background text-muted-foreground hover:bg-muted hover:text-foreground",
              ].join(" ")}
            >
              {draft.isDefault ? "✓ Set as default address" : "Set as default address"}
            </button>
          ) : null}

          <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
            <Button type="button" variant="outline" className="rounded-xl" onClick={() => setIsModalOpen(false)} disabled={isSaving}>
              Cancel
            </Button>
            <Button
              type="submit"
              disabled={
                isSaving ||
                !draft.line1.trim() ||
                !draft.city.trim() ||
                !draft.country.trim()
              }
              className="rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
            >
              {isSaving ? "Saving..." : editing ? "Save changes" : "Add address"}
            </Button>
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(deleteTarget)}
        title="Delete address?"
        description="This address will be removed from your account."
        confirmText="Delete"
        variant="danger"
        isLoading={isDeleting}
        onClose={() => setDeleteTarget(null)}
        onConfirm={onDelete}
      />
    </div>
  );
}
