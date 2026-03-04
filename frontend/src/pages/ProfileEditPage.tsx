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
          <div>
            <div className="text-2xl font-semibold">Edit profile</div>
            <div className="mt-1 text-sm text-muted-foreground">Update your personal details.</div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-xl bg-background">
            <Link to="/me">Back</Link>
          </Button>
        </div>
      </section>

      <Card className="overflow-hidden bg-background">
        <CardHeader className="relative">
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSave} className="space-y-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Username</label>
                <Input className="rounded-xl bg-background" value={username} onChange={(e) => setUsername(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input className="rounded-xl bg-background" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Full name</label>
                <Input className="rounded-xl bg-background" value={fullName} onChange={(e) => setFullName(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Phone</label>
                <Input className="rounded-xl bg-background" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
            </div>

            {error ? (
              <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div>
            ) : null}

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="outline" className="h-10 rounded-xl bg-background" onClick={() => navigate("/me")}>
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={!canSave}
                className="h-10 rounded-xl bg-primary text-primary-foreground hover:bg-primary/90"
              >
                {isSaving ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      <Card className="border-rose-500/20 bg-background">
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
            className="h-10 rounded-xl bg-rose-600 text-white hover:bg-rose-600/90"
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
