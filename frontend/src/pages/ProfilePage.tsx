import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/errors";
import { getMe } from "@/lib/userApi";

export default function ProfilePage() {
  const auth = useAuth();
  const toast = useToast();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [me, setMe] = useState<{ username?: string; email?: string; fullName?: string; phone?: string; enabled?: boolean } | null>(null);

  const title = useMemo(() => me?.fullName || me?.username || "Your profile", [me?.fullName, me?.username]);

  useEffect(() => {
    let alive = true;
    setIsLoading(true);
    setError(null);
    getMe()
      .then((data) => {
        if (!alive) return;
        setMe(data);
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

  if (!me) {
    return <EmptyState title="No profile data" description="We couldn’t find your profile." />;
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Account</div>
          <div className="text-3xl font-semibold tracking-tight">{title}</div>
          <div className="mt-1 text-sm text-muted-foreground">Manage your personal info, password, and addresses.</div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button asChild variant="outline" className="rounded-xl">
            <Link to="/me/edit">Edit profile</Link>
          </Button>
          <Button asChild variant="outline" className="rounded-xl">
            <Link to="/me/password">Change password</Link>
          </Button>
          <Button asChild className="rounded-xl bg-gradient-to-r from-indigo-500 via-fuchsia-500 to-emerald-500 text-white">
            <Link to="/me/addresses">Address book</Link>
          </Button>
        </div>
      </div>

      <Card className="overflow-hidden">
        <div className="pointer-events-none absolute inset-0 opacity-25 [background:radial-gradient(60%_60%_at_15%_20%,rgba(59,130,246,.22),transparent),radial-gradient(50%_60%_at_70%_40%,rgba(168,85,247,.18),transparent),radial-gradient(50%_70%_at_45%_90%,rgba(16,185,129,.12),transparent)]" />
        <CardHeader className="relative flex flex-row items-center justify-between">
          <CardTitle>Profile</CardTitle>
          <div className="rounded-full bg-primary/10 px-3 py-1 text-xs text-foreground ring-1 ring-primary/20">
            {me.enabled === false ? "Disabled" : "Active"}
          </div>
        </CardHeader>
        <CardContent className="relative grid gap-4 sm:grid-cols-2">
          <div className="rounded-2xl border bg-background/60 p-4 backdrop-blur">
            <div className="text-xs text-muted-foreground">Username</div>
            <div className="mt-1 text-base font-medium">{me.username || "—"}</div>
          </div>
          <div className="rounded-2xl border bg-background/60 p-4 backdrop-blur">
            <div className="text-xs text-muted-foreground">Email</div>
            <div className="mt-1 text-base font-medium">{me.email || "—"}</div>
          </div>
          <div className="rounded-2xl border bg-background/60 p-4 backdrop-blur">
            <div className="text-xs text-muted-foreground">Full name</div>
            <div className="mt-1 text-base font-medium">{me.fullName || "—"}</div>
          </div>
          <div className="rounded-2xl border bg-background/60 p-4 backdrop-blur">
            <div className="text-xs text-muted-foreground">Phone</div>
            <div className="mt-1 text-base font-medium">{me.phone || "—"}</div>
          </div>
        </CardContent>
      </Card>

      <Card className="shine pressable">
        <CardHeader>
          <CardTitle>Session</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
          <div className="text-sm text-muted-foreground">
            Signed in as <span className="font-medium text-foreground">{auth.user?.fullName || auth.user?.username || "User"}</span>
          </div>
          <Button
            variant="outline"
            className="rounded-xl"
            onClick={() => {
              auth.logout();
              toast.push({ variant: "success", title: "Logged out", message: "You have been logged out." });
            }}
          >
            Logout
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
