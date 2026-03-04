import { useEffect } from "react";

export default function Modal({
  isOpen,
  title,
  children,
  onClose,
}: {
  isOpen: boolean;
  title?: string;
  children: React.ReactNode;
  onClose: () => void;
}) {
  useEffect(() => {
    if (!isOpen) return;
    function onKeyDown(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [isOpen, onClose]);

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[90]">
      <div
        className="absolute inset-0 bg-black/40-sm animate-in fade-in"
        onClick={onClose}
        role="button"
        tabIndex={-1}
        aria-label="Close modal"
      />
      <div className="absolute inset-x-0 top-16 mx-auto w-[min(92vw,42rem)]">
        <div className="overflow-hidden rounded-xl border bg-background shadow-2xl animate-in fade-in zoom-in-95">
          {title ? (
            <div className="border-b px-5 py-4">
              <div className="text-sm font-semibold">{title}</div>
            </div>
          ) : null}
          <div className="px-5 py-4">{children}</div>
        </div>
      </div>
    </div>
  );
}

