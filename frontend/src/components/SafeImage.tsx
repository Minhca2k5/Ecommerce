import { getApiBaseUrl, isAbsoluteUrl } from "@/lib/env";
import { pickPlaceholder } from "@/lib/assets";
import { cn } from "@/lib/utils";
import { useEffect, useMemo, useState } from "react";

function resolveUrl(src: string) {
  if (!src.trim()) return "";
  if (isAbsoluteUrl(src)) return src;
  if (src.startsWith("//")) return `${window.location.protocol}${src}`;
  if (src.startsWith("/")) return `${getApiBaseUrl()}${src}`;
  return src;
}

export default function SafeImage({
  src,
  alt,
  className,
  fallbackKey,
}: {
  src: string | null | undefined;
  alt: string;
  className?: string;
  fallbackKey: string | number;
}) {
  const initial = useMemo(() => (src ? resolveUrl(src) : ""), [src]);
  const [current, setCurrent] = useState<string>(initial);

  const placeholder = useMemo(() => pickPlaceholder(fallbackKey), [fallbackKey]);

  useEffect(() => {
    setCurrent(initial);
  }, [initial]);

  return (
    <img
      src={current || placeholder}
      alt={alt}
      className={cn("block", className)}
      loading="lazy"
      onError={() => {
        if (current !== placeholder) setCurrent(placeholder);
      }}
    />
  );
}
