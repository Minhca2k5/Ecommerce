import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import { useToast } from "@/app/ToastProvider";
import { getErrorMessage } from "@/lib/errors";
import { requestRegisterOtp, verifyRegisterOtp } from "@/lib/authApi";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";

export default function RegisterPage() {
  const auth = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [otp, setOtp] = useState("");
  const [otpRequested, setOtpRequested] = useState(false);
  const [otpExpiresAt, setOtpExpiresAt] = useState<number | null>(null);
  const [secondsLeft, setSecondsLeft] = useState(0);
  const otpExpired = otpRequested && secondsLeft <= 0;
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const canSubmit = useMemo(() => {
    if (isLoading) return false;
    if (!username.trim() || !email.trim() || !password.trim()) return false;
    if (password.trim().length < 6) return false;
    if (password !== confirmPassword) return false;
    if (otpRequested && (!otp.trim() || otpExpired)) return false;
    return true;
  }, [confirmPassword, email, isLoading, otp, otpExpired, otpRequested, password, username]);

  useEffect(() => {
    if (!otpRequested || !otpExpiresAt) {
      setSecondsLeft(0);
      return;
    }
    const tick = () => {
      const left = Math.max(0, Math.ceil((otpExpiresAt - Date.now()) / 1000));
      setSecondsLeft(left);
    };
    tick();
    const id = window.setInterval(tick, 1000);
    return () => window.clearInterval(id);
  }, [otpRequested, otpExpiresAt]);

  if (auth.isAuthenticated) {
    navigate("/", { replace: true });
    return null;
  }

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);
    setIsLoading(true);
    try {
      if (!otpRequested) {
        await requestRegisterOtp({
          username: username.trim(),
          email: email.trim(),
          password,
          ...(fullName.trim() ? { fullName: fullName.trim() } : {}),
          ...(phone.trim() ? { phone: phone.trim() } : {}),
        });
        setOtpRequested(true);
        setOtpExpiresAt(Date.now() + 60_000);
        toast.push({ variant: "success", title: "Code sent", message: "Check your email for verification code." });
      } else {
        await verifyRegisterOtp({ email: email.trim(), code: otp.trim() });
        toast.push({ variant: "success", title: "Account created", message: "Email verified. You can login now." });
        navigate("/login", { replace: true });
      }
    } catch (err) {
      const message = getErrorMessage(err, "Register failed. Please check your input and try again.");
      setError(message);
      toast.push({ variant: "error", title: otpRequested ? "Verify failed" : "Request code failed", message });
    } finally {
      setIsLoading(false);
    }
  }

  return (
    <div className="mx-auto max-w-md space-y-4">
      <section className="page-section">
                <div className="relative">
          <div className="mt-1 text-2xl font-semibold">Create account</div>
          <div className="mt-1 text-sm text-muted-foreground">Two-step verification via email OTP.</div>
        </div>
      </section>

      <Card className="overflow-hidden bg-background">
        <CardHeader className="relative"><CardTitle>{otpRequested ? "Verify email" : "Create account"}</CardTitle></CardHeader>
        <CardContent className="relative">
          <form onSubmit={onSubmit} className="space-y-4">
            <Input value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Username" className="rounded-xl" />
            <Input value={email} onChange={(e) => setEmail(e.target.value)} placeholder="email@example.com" className="rounded-xl" />
            <Input value={fullName} onChange={(e) => setFullName(e.target.value)} placeholder="Full name (optional)" className="rounded-xl" />
            <Input value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="Phone (optional)" className="rounded-xl" />
            <Input value={password} onChange={(e) => setPassword(e.target.value)} type={showPassword ? "text" : "password"} placeholder="Password" className="rounded-xl" />
            <Input value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} type={showPassword ? "text" : "password"} placeholder="Confirm password" className="rounded-xl" />
            <button type="button" onClick={() => setShowPassword((v) => !v)} className="text-xs text-muted-foreground">{showPassword ? "Hide" : "Show"} password</button>

            {otpRequested ? (
              <>
                <Input value={otp} onChange={(e) => setOtp(e.target.value)} placeholder="6-digit verification code" className="rounded-xl" />
                <div className={`text-xs ${otpExpired ? "text-danger" : "text-muted-foreground"}`}>
                  {otpExpired
                    ? "Verification code expired. Please request a new code."
                    : `Code expires in: ${Math.floor(secondsLeft / 60)}:${String(secondsLeft % 60).padStart(2, "0")}`}
                </div>
              </>
            ) : null}
            {error ? <div className="rounded-xl border border-rose-500/20 bg-rose-500/10 px-3 py-2 text-sm text-rose-700">{error}</div> : null}

            <Button type="submit" disabled={!canSubmit} className="h-10 w-full rounded-xl bg-primary text-primary-foreground">
              {isLoading ? (otpRequested ? "Verifying..." : "Sending code...") : (otpRequested ? "Verify and create" : "Send verification code")}
            </Button>

            <div className="text-center text-sm text-muted-foreground">
              Already have an account? <Link className="text-primary hover:underline" to="/login">Login</Link>
            </div>
          </form>
        </CardContent>
      </Card>
    </div>
  );
}
