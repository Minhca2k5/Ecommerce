import { useMemo, useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { getAvailableRoles, setSelectedRole } from "@/lib/roleSelection";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

function defaultRouteForRole(role: string) {
  return role.toUpperCase() === "ADMIN" ? "/admin" : "/";
}

export default function LoginPage() {
  const auth = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSubmit = useMemo(() => username.trim().length > 0 && password.trim().length > 0 && !isLoading, [isLoading, password, username]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await auth.login(username.trim(), password);
      toast.push({
        variant: "success",
        title: "Logged in",
        message: `Welcome back, ${auth.user?.fullName || auth.user?.username || username}!`,
      });
      const rawFrom = (location.state as { from?: string } | null)?.from;
      const from = rawFrom && rawFrom !== "/choose-role" ? rawFrom : undefined;
      const roles = getAvailableRoles();
      if (roles.length > 1) {
        navigate("/choose-role", { replace: true, state: from ? { from } : null });
        return;
      }
      if (roles.length === 1) setSelectedRole(roles[0]);
      navigate(from || defaultRouteForRole(roles[0] ?? "USER"), { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Login failed. Please check your username/password.");
      setError(message);
      toast.push({ variant: "error", title: "Login failed", message });
    } finally {
      setIsLoading(false);
    }
  }

  if (auth.isAuthenticated) {
    return (
      <div className="mx-auto max-w-md space-y-4">
        <Card className="overflow-hidden">
          <CardHeader className="relative">
            <CardTitle>You're already logged in</CardTitle>
          </CardHeader>
          <CardContent className="relative space-y-3">
            <div className="text-sm text-muted-foreground">
              Signed in as <span className="font-medium text-foreground">{auth.user?.fullName || auth.user?.username || "User"}</span>.
            </div>
            <Button
              type="button"
              onClick={() => navigate("/", { replace: true })}
              className="w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90 active:scale-[0.99]"
            >
              Go to Home
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-md space-y-4">
      <section className="page-section">
        <div className="relative">
          <div className="mt-1 text-2xl font-semibold">Sign in</div>
        </div>
      </section>

      <Card className="overflow-hidden bg-background">
        <CardHeader className="relative">
          <CardTitle className="flex items-center justify-between">
            <span>Login</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Username</label>
              <Input
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                autoComplete="username"
                className="rounded-md bg-background"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Password</label>
              <div className="relative">
                <Input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  type={showPassword ? "text" : "password"}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  className="rounded-md pr-20 bg-background"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-2 top-1/2 -translate-y-1/2 rounded-lg px-2 py-1 text-xs text-muted-foreground hover:bg-muted hover:text-foreground"
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </div>

            {error ? (
              <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">
                {error}
              </div>
            ) : null}

            <Button
              type="submit"
              disabled={!canSubmit}
              className="h-10 w-full rounded-md bg-primary text-primary-foreground hover:bg-primary/90 active:scale-[0.99]"
            >
              {isLoading ? "Signing in..." : "Sign in"}
            </Button>

            <div className="text-center text-sm text-muted-foreground">
              No account?{" "}
              <Link className="text-primary hover:underline" to="/register">
                Create one
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

