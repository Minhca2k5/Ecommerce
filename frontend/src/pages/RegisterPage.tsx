import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useNotifications } from "@/app/NotificationProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

export default function RegisterPage() {
  const auth = useAuth();
  const notifications = useNotifications();
  const toast = useToast();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSubmit = useMemo(() => {
    if (isLoading) return false;
    if (!username.trim() || !email.trim() || !password.trim()) return false;
    if (password.trim().length < 6) return false;
    return password === confirmPassword;
  }, [confirmPassword, email, isLoading, password, username]);

  if (auth.isAuthenticated) {
    return (
      <div className="mx-auto max-w-md space-y-4">
        <Card className="overflow-hidden">
          <div className="pointer-events-none absolute inset-0 opacity-30 [background:radial-gradient(60%_60%_at_20%_20%,rgba(59,130,246,.25),transparent),radial-gradient(50%_60%_at_70%_50%,rgba(168,85,247,.20),transparent)]" />
          <CardHeader className="relative">
            <CardTitle>Account already active</CardTitle>
          </CardHeader>
          <CardContent className="relative space-y-3">
            <div className="text-sm text-muted-foreground">
              You're signed in as <span className="font-medium text-foreground">{auth.user?.fullName || auth.user?.username || "User"}</span>.
            </div>
            <Button
              type="button"
              onClick={() => navigate("/", { replace: true })}
              className="w-full rounded-xl bg-gradient-to-r from-indigo-500 via-fuchsia-500 to-emerald-500 text-white hover:opacity-95 active:scale-[0.99]"
            >
              Go to Home
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      await auth.register({
        username: username.trim(),
        email: email.trim(),
        password,
        ...(fullName.trim() ? { fullName: fullName.trim() } : {}),
        ...(phone.trim() ? { phone: phone.trim() } : {}),
      });
      toast.push({ variant: "success", title: "Account created", message: "Welcome! Your account is ready." });
      notifications.push({
        type: "SYSTEM",
        title: "Welcome!",
        message: `Account created successfully. You're now signed in as ${auth.user?.username || username.trim()}.`,
        referenceType: "USER",
      });
      navigate("/", { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Register failed. Please check your input and try again.");
      setError(message);
      toast.push({ variant: "error", title: "Register failed", message });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-md space-y-4">
      <section className="relative overflow-hidden rounded-3xl border bg-background/70 p-6 shadow-sm backdrop-blur">
        <div className="pointer-events-none absolute inset-0 bg-gradient-to-br from-primary/20 via-fuchsia-500/10 to-emerald-500/10" />
        <div className="relative">
          <div className="text-sm text-muted-foreground">Welcome</div>
          <div className="mt-1 text-3xl font-semibold tracking-tight">Create account</div>
          <div className="mt-1 text-sm text-muted-foreground">Join to save wishlist, place orders, and track payments.</div>
        </div>
      </section>

      <Card className="overflow-hidden bg-background/70 backdrop-blur">
        <div className="pointer-events-none absolute inset-0 opacity-30 [background:radial-gradient(60%_60%_at_20%_20%,rgba(59,130,246,.25),transparent),radial-gradient(50%_60%_at_70%_50%,rgba(168,85,247,.20),transparent)]" />
        <CardHeader className="relative">
          <CardTitle className="flex items-center justify-between">
            <span>Create account</span>
            <span className="text-xs text-muted-foreground">Milestone M5</span>
          </CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Username</label>
                <Input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="username" className="rounded-xl bg-background/70 backdrop-blur" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Email</label>
                <Input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email@example.com" className="rounded-xl bg-background/70 backdrop-blur" />
              </div>
            </div>

            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <label className="text-sm font-medium">Full name (optional)</label>
                <Input value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Full name" className="rounded-xl bg-background/70 backdrop-blur" />
              </div>
              <div className="space-y-2">
                <label className="text-sm font-medium">Phone (optional)</label>
                <Input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="+84..." className="rounded-xl bg-background/70 backdrop-blur" />
              </div>
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Password</label>
              <div className="relative">
                <Input
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  type={showPassword ? "text" : "password"}
                  placeholder="At least 6 characters"
                  autoComplete="new-password"
                  className="rounded-xl pr-20 bg-background/70 backdrop-blur"
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

            <div className="space-y-2">
              <label className="text-sm font-medium">Confirm password</label>
              <Input
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                type={showPassword ? "text" : "password"}
                placeholder="Re-enter your password"
                autoComplete="new-password"
                className="rounded-xl bg-background/70 backdrop-blur"
              />
              {confirmPassword.length > 0 && confirmPassword !== password ? (
                <div className="text-xs text-rose-600">Passwords do not match.</div>
              ) : null}
            </div>

            {error ? (
              <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">
                {error}
              </div>
            ) : null}

            <Button
              type="submit"
              disabled={!canSubmit}
              className="h-10 w-full rounded-xl bg-gradient-to-r from-indigo-500 via-fuchsia-500 to-emerald-500 text-white hover:opacity-95 active:scale-[0.99]"
            >
              {isLoading ? "Creating..." : "Create account"}
            </Button>

            <div className="text-center text-sm text-muted-foreground">
              Already have an account?{" "}
              <Link className="text-primary hover:underline" to="/login">
                Login
              </Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
