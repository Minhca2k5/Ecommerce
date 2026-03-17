import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import EmptyState from "@/components/EmptyState";
import LoadingCard from "@/components/LoadingCard";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { getErrorMessage } from "@/lib/errors";
import { getMe } from "@/lib/userApi";

export default function AdminProfilePage() {
  const auth = useAuth();
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
      .catch((e) => alive && setError(getErrorMessage(e, "Failed to load profile.")))
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

  if (error) return <EmptyState title="Couldn't load profile" description={error} />;
  if (!me) return <EmptyState title="No profile data" description="We couldn't find your profile." />;

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <div className="text-sm text-muted-foreground">Admin</div>
          <div className="text-2xl font-semibold">{title}</div>
          <div className="mt-1 text-sm text-muted-foreground">Account settings while in admin mode.</div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button asChild variant="outline" className="rounded-md">
            <Link to="/admin/profile/edit">Edit profile</Link>
          </Button>
          <Button asChild variant="outline" className="rounded-md">
            <Link to="/admin/profile/password">Change password</Link>
          </Button>
        </div>
      </div>

      <Card className="overflow-hidden">
        <CardHeader className="relative flex flex-row items-center justify-between">
          <CardTitle>Profile</CardTitle>
          <div className="rounded-full bg-primary/10 px-3 py-1 text-sm text-foreground ring-1 ring-primary/20">{me.enabled === false ? "Disabled" : "Active"}</div>
        </CardHeader>
        <CardContent className="relative grid gap-4 sm:grid-cols-2">
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm text-muted-foreground">Username</div>
            <div className="mt-1 text-base font-medium">{me.username || "-"}</div>
          </div>
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm text-muted-foreground">Email</div>
            <div className="mt-1 text-base font-medium">{me.email || "-"}</div>
          </div>
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm text-muted-foreground">Full name</div>
            <div className="mt-1 text-base font-medium">{me.fullName || "-"}</div>
          </div>
          <div className="rounded-md border bg-background p-4">
            <div className="text-sm text-muted-foreground">Phone</div>
            <div className="mt-1 text-base font-medium">{me.phone || "-"}</div>
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
          <Button variant="outline" className="rounded-md" asChild>
            <Link to="/admin">Back to admin</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}


