import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import ConfirmDialog from "@/components/ConfirmDialog";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import { deleteMe, getMe, updateMe } from "@/lib/userApi";

export default function ProfileEditPage() {
  const auth = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");

  const [isDeleteOpen, setIsDeleteOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);

  const canSave = useMemo(() => {
    if (isSaving) return false;
    return Boolean(username.trim() || email.trim() || fullName.trim() || phone.trim());
  }, [email, fullName, isSaving, phone, username]);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);
    getMe()
      .then((data) => {
        if (!alive) return;
        setUsername(data.username ?? "");
        setEmail(data.email ?? "");
        setFullName(data.fullName ?? "");
        setPhone(data.phone ?? "");
      })
      .catch((e) => {
        if (!alive) return;
        setError(getErrorMessage(e, "Failed to load profile."));
      })
      .finally(() => alive && setIsLoading(false));
    return () => {
      alive = false;
    };
  }, []);

  async function onSave(e: React.FormEvent) {
    e.preventDefault();
    setIsSaving(true);
    setError(null);
    try {
      const updated = await updateMe({
        username: username.trim() || undefined,
        email: email.trim() || undefined,
        fullName: fullName.trim() || undefined,
        phone: phone.trim() || undefined,
      });
      await auth.refreshMe();
      toast.push({ variant: "success", title: "Saved", message: "Profile updated successfully." });
      setUsername(updated.username ?? username);
      setEmail(updated.email ?? email);
      setFullName(updated.fullName ?? fullName);
      setPhone(updated.phone ?? phone);
      navigate("/me", { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Failed to update profile.");
      setError(message);
      toast.push({ variant: "error", title: "Save failed", message });
    } finally {
      setIsSaving(false);
    }
  }

  async function onDeleteAccount() {
    setIsDeleting(true);
    try {
      await deleteMe();
      auth.logout();
      toast.push({ variant: "success", title: "Account deleted", message: "Your account has been removed." });
      navigate("/", { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Failed to delete account.");
      toast.push({ variant: "error", title: "Delete failed", message });
    } finally {
      setIsDeleting(false);
      setIsDeleteOpen(false);
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

  if (error && !isSaving) {
    return (
      <EmptyState
        title="Couldn’t load profile"
        description={error}
        action={
          <Button onClick={() => window.location.reload()} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white">
            Retry
          </Button>
        }
      />
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Account</div>
          <div className="text-3xl font-semibold tracking-tight">Edit profile</div>
          <div className="mt-1 text-sm text-muted-foreground">Update your personal details.</div>
        </div>
        <Button asChild variant="outline" className="rounded-xl">
          <Link to="/me">Back</Link>
        </Button>
      </div>

      <Card className="overflow-hidden">
        <div className="pointer-events-none absolute inset-0 opacity-25 [background:radial-gradient(60%_60%_at_20%_20%,rgba(168,85,247,.18),transparent),radial-gradient(50%_60%_at_70%_40%,rgba(16,185,129,.14),transparent)]" />
        <CardHeader className="relative">
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSave} className="space-y-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Username</label>
                <Input className="rounded-xl" value={username} onChange={(e) => setUsername(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input className="rounded-xl" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Full name</label>
                <Input className="rounded-xl" value={fullName} onChange={(e) => setFullName(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Phone</label>
                <Input className="rounded-xl" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
            </div>

            {error ? (
              <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div>
            ) : null}

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="outline" className="rounded-xl" onClick={() => navigate("/me")}>
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={!canSave}
                className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95"
              >
                {isSaving ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card className="border-rose-500/20">
        <CardHeader>
          <CardTitle className="text-rose-700">Danger zone</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-sm text-muted-foreground">
            Delete your account permanently. This action cannot be undone.
          </div>
          <Button
            type="button"
            onClick={() => setIsDeleteOpen(true)}
            className="rounded-xl bg-rose-600 text-white hover:bg-rose-600/90"
          >
            Delete account
          </Button>
        </CardContent>
      </Card>

      <ConfirmDialog
        isOpen={isDeleteOpen}
        title="Delete account?"
        description="This will permanently remove your account and all related data. Continue?"
        confirmText="Delete"
        variant="danger"
        isLoading={isDeleting}
        onClose={() => setIsDeleteOpen(false)}
        onConfirm={onDeleteAccount}
      />
    </div>
  );
}

