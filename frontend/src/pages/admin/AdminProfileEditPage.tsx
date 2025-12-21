import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import { getMe, updateMe } from "@/lib/userApi";

export default function AdminProfileEditPage() {
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
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load profile.")))
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
      await updateMe({
        username: username.trim() || undefined,
        email: email.trim() || undefined,
        fullName: fullName.trim() || undefined,
        phone: phone.trim() || undefined,
      });
      await auth.refreshMe();
      toast.push({ variant: "success", title: "Saved", message: "Profile updated successfully." });
      navigate("/admin/profile", { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Failed to update profile.");
      setError(message);
      toast.push({ variant: "error", title: "Save failed", message });
    } finally {
      setIsSaving(false);
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

  if (error && !isSaving) return <EmptyState title="Couldn't load profile" description={error} />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Admin</div>
          <div className="text-3xl font-semibold tracking-tight">Edit profile</div>
          <div className="mt-1 text-sm text-muted-foreground">Update your personal details.</div>
        </div>
        <Button asChild variant="outline" className="rounded-xl">
          <Link to="/admin/profile">Back</Link>
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

            {error ? <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div> : null}

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="outline" className="rounded-xl" onClick={() => navigate("/admin/profile")}>
                Cancel
              </Button>
              <Button type="submit" disabled={!canSave} className="rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95">
                {isSaving ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

