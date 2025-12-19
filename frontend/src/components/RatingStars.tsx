import { cn } from "@/lib/utils";

export default function RatingStars({
  rating,
  className,
}: {
  rating: number | null | undefined;
  className?: string;
}) {
  const safeRating = rating ?? 0;
  const full = Math.floor(safeRating);
  const half = safeRating - full >= 0.5;

  return (
    <div className={cn("flex items-center gap-0.5", className)}>
      {Array.from({ length: 5 }).map((_, i) => {
        const index = i + 1;
        const filled = index <= full;
        const halfFilled = !filled && half && index === full + 1;

        return (
          <span
            key={index}
            className={cn(
              "text-[13px]",
              filled || halfFilled ? "text-primary" : "text-muted-foreground/40"
            )}
            aria-hidden="true"
          >
            ★
          </span>
        );
      })}
    </div>
  );
}

