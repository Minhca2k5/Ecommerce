import { cn } from "@/lib/utils";

function Icon({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <span
      className={cn(
        "inline-flex h-11 w-11 items-center justify-center rounded-2xl border bg-background/70 backdrop-blur shadow-sm",
        className
      )}
      aria-hidden="true"
    >
      {children}
    </span>
  );
}

export default function CategoryIcon({
  name,
  className,
}: {
  name:
    | "bolt"
    | "book"
    | "shirt"
    | "laptop"
    | "phone"
    | "headphones"
    | "camera"
    | "tablet"
    | "sparkles"
    | "tag";
  className?: string;
}) {
  const base = "h-5 w-5 text-foreground/80";

  switch (name) {
    case "bolt":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M13 2L3 14h8l-1 8 11-14h-8l0-6z" />
          </svg>
        </Icon>
      );
    case "book":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20" />
            <path d="M4 4.5A2.5 2.5 0 0 1 6.5 7H20" />
            <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z" />
          </svg>
        </Icon>
      );
    case "shirt":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M8 4l4 2 4-2 4 4-3 3v10H7V11L4 8l4-4z" />
          </svg>
        </Icon>
      );
    case "laptop":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M4 5h16v10H4z" />
            <path d="M2 19h20" />
            <path d="M6 19l1-4h10l1 4" />
          </svg>
        </Icon>
      );
    case "phone":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M8 2h8v20H8z" />
            <path d="M10 5h4" />
            <path d="M12 19h.01" />
          </svg>
        </Icon>
      );
    case "headphones":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M4 13v-1a8 8 0 0 1 16 0v1" />
            <path d="M4 13a2 2 0 0 0 2 2h1v-4H6a2 2 0 0 0-2 2z" />
            <path d="M20 13a2 2 0 0 1-2 2h-1v-4h1a2 2 0 0 1 2 2z" />
          </svg>
        </Icon>
      );
    case "camera":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M4 7h4l2-2h4l2 2h4v12H4z" />
            <circle cx="12" cy="13" r="3" />
          </svg>
        </Icon>
      );
    case "tablet":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="6" y="2" width="12" height="20" rx="2" />
            <path d="M12 18h.01" />
          </svg>
        </Icon>
      );
    case "sparkles":
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M12 2l1.2 4.2L17 7.5l-3.8 1.3L12 13l-1.2-4.2L7 7.5l3.8-1.3L12 2z" />
            <path d="M19 12l.7 2.4L22 15l-2.3.6L19 18l-.7-2.4L16 15l2.3-.6L19 12z" />
            <path d="M5 13l.8 2.8L8 16l-2.2.6L5 19l-.8-2.4L2 16l2.2-.2L5 13z" />
          </svg>
        </Icon>
      );
    default:
      return (
        <Icon className={className}>
          <svg viewBox="0 0 24 24" className={base} fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M20 12H4" />
            <path d="M6 7h12" />
            <path d="M8 17h8" />
          </svg>
        </Icon>
      );
  }
}

