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
          <div className="text-2xl font-semibold">Edit profile</div>
          <div className="mt-1 text-sm text-muted-foreground">Update your personal details.</div>
        </div>
        <Button asChild variant="outline" className="rounded-md">
          <Link to="/admin/profile">Back</Link>
        </Button>
      </div>

      <Card className="overflow-hidden">
        <CardHeader className="relative">
          <CardTitle>Details</CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSave} className="space-y-4">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Username</label>
                <Input className="rounded-md" value={username} onChange={(e) => setUsername(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input className="rounded-md" value={email} onChange={(e) => setEmail(e.target.value)} />
              </div>
            </div>

            <div className="grid gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Full name</label>
                <Input className="rounded-md" value={fullName} onChange={(e) => setFullName(e.target.value)} />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Phone</label>
                <Input className="rounded-md" value={phone} onChange={(e) => setPhone(e.target.value)} />
              </div>
            </div>

            {error ? <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div> : null}

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="outline" className="rounded-md" onClick={() => navigate("/admin/profile")}>
                Cancel
              </Button>
              <Button type="submit" disabled={!canSave} className="rounded-md bg-primary text-primary-foreground hover:bg-primary/90">
                {isSaving ? "Saving..." : "Save changes"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}


