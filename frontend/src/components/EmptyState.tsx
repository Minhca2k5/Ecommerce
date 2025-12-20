import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function EmptyState({
  title,
  description,
  action,
}: {
  title: string;
  description?: string;
  action?: React.ReactNode;
}) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      {description || action ? (
        <CardContent className="space-y-4">
          {description ? <div className="text-sm text-muted-foreground">{description}</div> : null}
          {action ? <div>{action}</div> : null}
        </CardContent>
      ) : null}
    </Card>
  );
}
