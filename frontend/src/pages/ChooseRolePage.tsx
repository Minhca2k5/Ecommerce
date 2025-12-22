import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import EmptyState from "@/components/EmptyState";
import { getAvailableRoles, getSelectedRole, setSelectedRole } from "@/lib/roleSelection";

function defaultRouteForRole(role: string) {
  return role.toUpperCase() === "ADMIN" ? "/admin" : "/";
}

export default function ChooseRolePage() {
  const navigate = useNavigate();
  const location = useLocation();

  const availableRoles = useMemo(() => getAvailableRoles(), []);
  const [selected, setSelected] = useState(() => getSelectedRole() ?? availableRoles[0] ?? "USER");
  const rawFrom = (location.state as { from?: string } | null)?.from;
  const from = rawFrom && rawFrom !== "/" && rawFrom !== "/choose-role" ? rawFrom : undefined;

  useEffect(() => {
    if (availableRoles.length === 1) {
      setSelectedRole(availableRoles[0]);
      navigate(defaultRouteForRole(availableRoles[0]), { replace: true });
    }
  }, [availableRoles, navigate]);

  if (!availableRoles.length) {
    return <EmptyState title="No roles found" description="Please sign in again." />;
  }

  if (availableRoles.length === 1) {
    return null;
  }

  return (
    <div className="mx-auto max-w-lg space-y-4">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative">
          <div className="text-sm text-muted-foreground">Account</div>
          <div className="mt-1 text-3xl font-semibold tracking-tight">Choose role</div>
          <div className="mt-1 text-sm text-muted-foreground">This account has multiple roles. Select how you want to use the app.</div>
        </div>
      </section>

      <Card className="overflow-hidden bg-background/70 backdrop-blur">
        <div className="pointer-events-none absolute inset-0 opacity-30 [background:radial-gradient(60%_60%_at_30%_20%,rgba(168,85,247,.25),transparent),radial-gradient(50%_60%_at_70%_40%,rgba(16,185,129,.18),transparent)]" />
        <CardHeader className="relative">
          <CardTitle>Role selection</CardTitle>
          <div className="mt-1 text-sm text-muted-foreground">You can switch roles later from the header menu.</div>
        </CardHeader>
        <CardContent className="relative space-y-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">Role</label>
            <select value={selected} onChange={(e) => setSelected(e.target.value)} className="h-10 w-full rounded-xl border bg-background/70 px-3 text-sm shadow-sm backdrop-blur focus:outline-none focus:ring-2 focus:ring-primary/30">
              {availableRoles.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          <Button
            className="h-10 w-full rounded-xl bg-gradient-to-r from-primary via-fuchsia-500 to-emerald-500 text-white hover:opacity-95 active:scale-[0.99]"
            onClick={() => {
              const role = selected.trim() || availableRoles[0];
              setSelectedRole(role);
              const target = from || defaultRouteForRole(role);
              navigate(target, { replace: true });
            }}
          >
            Continue
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
