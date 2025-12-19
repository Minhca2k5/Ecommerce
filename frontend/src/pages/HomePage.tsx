import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { apiGet } from "@/lib/apiClient";
import { useEffect, useState } from "react";

export default function HomePage() {
  const [data, setData] = useState<unknown>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    async function run() {
      try {
        setIsLoading(true);
        setError(null);
        const result = await apiGet<unknown>("/api/public/home");
        if (!isMounted) return;
        setData(result);
      } catch (e) {
        if (!isMounted) return;
        setError(e instanceof Error ? e.message : "Unknown error");
      } finally {
        if (!isMounted) return;
        setIsLoading(false);
      }
    }

    void run();
    return () => {
      isMounted = false;
    };
  }, []);

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Home</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="text-sm text-muted-foreground">
            Milestone M3: gọi API `GET /api/public/home` và render dữ liệu thật.
          </div>

          {isLoading ? (
            <div className="text-sm">Loading...</div>
          ) : error ? (
            <div className="text-sm text-destructive">{error}</div>
          ) : (
            <pre className="max-h-[60vh] overflow-auto rounded-md border bg-muted p-3 text-xs">
              {JSON.stringify(data, null, 2)}
            </pre>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
