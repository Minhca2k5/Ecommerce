import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";

export default function LoginPage() {
  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Login</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Milestone M5 sẽ triển khai login + refresh token.
        </CardContent>
      </Card>
    </div>
  );
}

