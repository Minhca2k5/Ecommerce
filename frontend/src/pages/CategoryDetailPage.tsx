import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Link, useParams } from "react-router-dom";

export default function CategoryDetailPage() {
  const { categoryId } = useParams();

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Category Detail</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-sm text-muted-foreground">
          <div>
            categoryId: <span className="text-foreground">{categoryId}</span>
          </div>
          <Button asChild variant="outline">
            <Link to="/products">Go to products</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

