import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function EmptyState({ title, description }: { title: string; description?: string }) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      {description ? (
        <CardContent className="text-sm text-muted-foreground">{description}</CardContent>
      ) : null}
    </Card>
  );
}

