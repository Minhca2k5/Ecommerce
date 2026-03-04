import { Button } from "@/components/ui/button";
import Modal from "@/components/Modal";

export default function ConfirmDialog({
  isOpen,
  title,
  description,
  confirmText = "Confirm",
  cancelText = "Cancel",
  variant = "danger",
  onConfirm,
  onClose,
  isLoading = false,
}: {
  isOpen: boolean;
  title: string;
  description?: string;
  confirmText?: string;
  cancelText?: string;
  variant?: "danger" | "default";
  isLoading?: boolean;
  onConfirm: () => void | Promise<void>;
  onClose: () => void;
}) {
  return (
    <Modal isOpen={isOpen} title={title} onClose={onClose}>
      <div className="space-y-4">
        {description ? <div className="text-sm text-muted-foreground">{description}</div> : null}
        <div className="flex flex-col-reverse gap-2 sm:flex-row sm:justify-end">
          <Button variant="outline" className="rounded-xl" onClick={onClose} disabled={isLoading}>
            {cancelText}
          </Button>
          <Button
            className={[
              "rounded-xl",
              variant === "danger"
                ? "bg-rose-600 text-white hover:bg-rose-600/90"
                : "bg-primary text-primary-foreground hover:bg-primary/90",
            ].join(" ")}
            onClick={onConfirm}
            disabled={isLoading}
          >
            {isLoading ? "Working..." : confirmText}
          </Button>
        </div>
      </div>
    </Modal>
  );
}

