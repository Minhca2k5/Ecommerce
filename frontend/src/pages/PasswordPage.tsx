import { useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useToast } from "@/app/ToastProvider";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { getErrorMessage } from "@/lib/errors";
import { changePassword } from "@/lib/userApi";

export default function PasswordPage() {
  const toast = useToast();
  const navigate = useNavigate();

  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSubmit = useMemo(() => {
    if (isSaving) return false;
    if (!oldPassword.trim() || !newPassword.trim() || !confirmPassword.trim()) return false;
    if (newPassword.length < 6) return false;
    return newPassword === confirmPassword;
  }, [confirmPassword, isSaving, newPassword, oldPassword]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsSaving(true);
    try {
      await changePassword({ oldPassword, newPassword, confirmPassword });
      toast.push({ variant: "success", title: "Password updated", message: "Your password has been changed." });
      navigate("/me", { replace: true });
    } catch (err) {
      const message = getErrorMessage(err, "Failed to change password.");
      setError(message);
      toast.push({ variant: "error", title: "Change failed", message });
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <div className="mx-auto max-w-xl space-y-6">
      <section className="page-section">
        <div className="relative flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <div className="text-2xl font-semibold">Change password</div>
            <div className="mt-1 text-sm text-muted-foreground">Keep your account secure with a strong password.</div>
          </div>
          <Button asChild variant="outline" className="h-10 rounded-md bg-background">
            <Link to="/me">Back</Link>
          </Button>
        </div>
      </section>

      <Card className="overflow-hidden bg-background">
        <CardHeader className="relative">
          <CardTitle>Password</CardTitle>
        </CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSubmit} className="space-y-4">
            <div className="space-y-2">
              <label className="text-sm font-medium">Old password</label>
              <Input
                className="rounded-md bg-background"
                value={oldPassword}
                onChange={(e) => setOldPassword(e.target.value)}
                type={showPassword ? "text" : "password"}
                autoComplete="current-password"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">New password</label>
              <Input
                className="rounded-md bg-background"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                type={showPassword ? "text" : "password"}
                autoComplete="new-password"
                placeholder="At least 6 characters"
              />
            </div>

            <div className="space-y-2">
              <label className="text-sm font-medium">Confirm password</label>
              <Input
                className="rounded-md bg-background"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
                type={showPassword ? "text" : "password"}
                autoComplete="new-password"
                placeholder="Re-enter new password"
              />
              {confirmPassword.length > 0 && confirmPassword !== newPassword ? (
                <div className="text-xs text-danger">Passwords do not match.</div>
              ) : null}
            </div>

            <div className="flex items-center gap-2">
              <button
                type="button"
                onClick={() => setShowPassword((v) => !v)}
                className="pressable rounded-md border bg-background px-3 py-2 text-sm text-muted-foreground shadow-sm hover:bg-muted hover:text-foreground"
              >
                {showPassword ? "Hide passwords" : "Show passwords"}
              </button>
            </div>

            {error ? (
              <div className="rounded-md border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div>
            ) : null}

            <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
              <Button type="button" variant="outline" className="h-10 rounded-md bg-background" onClick={() => navigate("/me")}>
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={!canSubmit}
                className="h-10 rounded-md bg-primary text-primary-foreground hover:bg-primary/90"
              >
                {isSaving ? "Updating..." : "Update password"}
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}

