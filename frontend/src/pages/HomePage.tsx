import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function HomePage() {
  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Home</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Milestone M2: routing + layout. M3 sẽ bắt đầu gọi API.
        </CardContent>
      </Card>
    </div>
  );
}

