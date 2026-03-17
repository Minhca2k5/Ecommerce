import { getApiBaseUrl, isAbsoluteUrl } from "@/lib/env";
import { pickPlaceholder } from "@/lib/assets";
import { cn } from "@/lib/utils";
import { useEffect, useMemo, useState } from "react";

function resolveUrl(src: string) {
  const trimmed = src.trim();
  if (!trimmed) return "";

  const normalized = trimmed.replace(/^['"]+|['"]+$/g, "").replace(/\\+/g, "/");
  if (!normalized) return "";

  if (
    isAbsoluteUrl(normalized) ||
    normalized.startsWith("data:") ||
    normalized.startsWith("blob:")
  ) {
    return normalized;
  }

  if (normalized.startsWith("//")) {
    return `${window.location.protocol}${normalized}`;
  }

  const base = getApiBaseUrl();
  if (normalized.startsWith("/")) {
    return `${base}${normalized}`;
  }

  const slashNormalized = normalized.replace(/^\.\//, "").replace(/^\\+/, "");
  return `${base}/${slashNormalized}`;
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
