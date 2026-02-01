import { createContext, useCallback, useContext, useEffect, useMemo, useState } from "react";

export type ToastVariant = "default" | "success" | "error";

export type ToastItem = {
  id: string;
  title?: string;
  message: string;
  variant: ToastVariant;
};

type ToastContextValue = {
  push: (toast: Omit<ToastItem, "id">) => void;
};

const ToastContext = createContext<ToastContextValue | null>(null);

function randomId() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<ToastItem[]>([]);

  const remove = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const push = useCallback((toast: Omit<ToastItem, "id">) => {
    const id = randomId();
    const next: ToastItem = { id, ...toast };
    setToasts((prev) => [next, ...prev].slice(0, 4));
    window.setTimeout(() => remove(id), 3500);
  }, [remove]);

  useEffect(() => {
    const handler = (event: Event) => {
      const detail = (event as CustomEvent).detail as { title?: string; message?: string; variant?: ToastVariant };
      if (!detail?.message) return;
      push({ title: detail.title, message: detail.message, variant: detail.variant ?? "default" });
    };
    window.addEventListener("app:toast", handler);
    return () => window.removeEventListener("app:toast", handler);
  }, [push]);

  const value = useMemo(() => ({ push }), [push]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="pointer-events-none fixed inset-x-0 top-16 z-[100] flex flex-col items-center gap-2 px-4">
        {toasts.map((t) => (
          <div
            key={t.id}
            className={[
              "pointer-events-auto w-full max-w-md overflow-hidden rounded-2xl border bg-background/80 shadow-lg backdrop-blur",
              "animate-in fade-in slide-in-from-top-2 duration-200",
              t.variant === "success" ? "border-emerald-500/30" : "",
              t.variant === "error" ? "border-rose-500/30" : "",
            ].join(" ")}
          >
            <div className="flex items-start gap-3 p-4">
              <div
                className={[
                  "mt-0.5 h-2.5 w-2.5 rounded-full",
                  t.variant === "success" ? "bg-emerald-500" : "",
                  t.variant === "error" ? "bg-rose-500" : "bg-primary",
                ].join(" ")}
              />
              <div className="min-w-0 flex-1">
                {t.title ? <div className="text-sm font-medium">{t.title}</div> : null}
                <div className="text-sm text-muted-foreground">{t.message}</div>
              </div>
              <button
                type="button"
                onClick={() => remove(t.id)}
                className="rounded-lg px-2 py-1 text-muted-foreground hover:bg-muted hover:text-foreground"
                aria-label="Dismiss"
              >
                ✕
              </button>
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error("useToast must be used within ToastProvider");
  return ctx;
}
