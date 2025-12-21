import EmptyState from "@/components/EmptyState";
import { Card, CardContent } from "@/components/ui/card";

export default function AdminPlaceholder({ title, message }: { title: string; message?: string }) {
  return (
    <Card className="border bg-background/75 shadow-sm backdrop-blur">
      <CardContent className="p-10">
        <EmptyState title={title} description={message ?? "This admin module will be implemented in M9."} />
      </CardContent>
    </Card>
  );
}
