import { createContext, useContext, useEffect, useMemo } from "react";

type Theme = "light" | "dark" | "system";

type ThemeContextValue = {
  theme: Theme;
  resolvedTheme: "light" | "dark";
  setTheme: (theme: Theme) => void;
  toggle: () => void;
};

const ThemeContext = createContext<ThemeContextValue | null>(null);

function applyThemeClass() {
  const root = document.documentElement;
  root.classList.remove("dark");
}

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const theme: Theme = "light";
  const resolvedTheme: "light" | "dark" = "light";

  useEffect(() => {
    applyThemeClass();
  }, []);

  const value = useMemo<ThemeContextValue>(
    () => ({
      theme,
      resolvedTheme,
      setTheme: () => undefined,
      toggle: () => undefined,
    }),
    [resolvedTheme, theme],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useTheme must be used within ThemeProvider");
  return ctx;
}
