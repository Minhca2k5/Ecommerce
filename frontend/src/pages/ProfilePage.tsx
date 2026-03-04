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
          <Button onClick={() => window.location.reload()} className="rounded-xl bg-primary text-primary-foreground">
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
        <div className="text-2xl font-semibold">{title}</div>
        <div className="flex flex-wrap gap-2">
          <Button asChild variant="outline" className="rounded-xl">
            <Link to="/me/edit">Edit profile</Link>
          </Button>
          <Button asChild variant="outline" className="rounded-xl">
            <Link to="/me/password">Change password</Link>
          </Button>
          <Button asChild className="rounded-xl bg-primary text-primary-foreground">
            <Link to="/me/addresses">Address book</Link>
          </Button>
        </div>
      </div>

      <Card className="overflow-hidden">
        <CardHeader className="relative flex flex-row items-center justify-between">
          <CardTitle>Profile</CardTitle>
          <div className="rounded-full bg-primary/10 px-3 py-1 text-xs text-foreground ring-1 ring-primary/20">
            {me.enabled === false ? "Disabled" : "Active"}
          </div>
        </CardHeader>
        <CardContent className="relative grid gap-4 sm:grid-cols-2">
          <div className="rounded-xl border bg-background p-4">
            <div className="text-xs text-muted-foreground">Username</div>
            <div className="mt-1 text-base font-medium">{me.username || "—"}</div>
          </div>
          <div className="rounded-xl border bg-background p-4">
            <div className="text-xs text-muted-foreground">Email</div>
            <div className="mt-1 text-base font-medium">{me.email || "—"}</div>
          </div>
          <div className="rounded-xl border bg-background p-4">
            <div className="text-xs text-muted-foreground">Full name</div>
            <div className="mt-1 text-base font-medium">{me.fullName || "—"}</div>
          </div>
          <div className="rounded-xl border bg-background p-4">
            <div className="text-xs text-muted-foreground">Phone</div>
            <div className="mt-1 text-base font-medium">{me.phone || "—"}</div>
          </div>
        </CardContent>
      </Card>

      <Card className="pressable">
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
