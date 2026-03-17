import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>404 - Not Found</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-sm text-muted-foreground">
          <div>The page you are looking for does not exist.</div>
          <Button asChild>
            <Link to="/">Go back home</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
