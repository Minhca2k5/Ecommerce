import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Link, useParams } from "react-router-dom";

export default function ProductDetailPage() {
  const { productId } = useParams();

  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Product Detail</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3 text-sm text-muted-foreground">
          <div>
            productId: <span className="text-foreground">{productId}</span>
          </div>
          <Button asChild variant="outline">
            <Link to="/products">Back to products</Link>
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}

