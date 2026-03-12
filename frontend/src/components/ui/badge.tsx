import * as React from "react";

import { cn } from "@/lib/utils";

function Badge({
  className,
  variant = "default",
  ...props
}: React.HTMLAttributes<HTMLDivElement> & {
  variant?: "default" | "secondary" | "outline";
}) {
  return (
    <div
      className={cn(
        "inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors shadow-[inset_0_1px_0_rgba(255,255,255,0.6)]",
        variant === "default" && "border-transparent bg-primary text-primary-foreground",
        variant === "secondary" &&
          "border-transparent bg-secondary text-secondary-foreground",
        variant === "outline" && "border-primary/25 bg-white/70 text-foreground",
        className
      )}
      {...props}
    />
  );
}

export { Badge };
