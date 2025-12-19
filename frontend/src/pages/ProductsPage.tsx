import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Link } from "react-router-dom";

export default function ProductsPage() {
  return (
    <div className="space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Products</CardTitle>
        </CardHeader>
        <CardContent className="flex flex-col gap-3 text-sm text-muted-foreground">
          <div>Trang list sản phẩm (M4 sẽ gọi API thật).</div>
          <div className="flex flex-wrap gap-2">
            <Button asChild variant="secondary">
              <Link to="/products/1">Open product #1</Link>
            </Button>
            <Button asChild variant="outline">
              <Link to="/categories/1">Open category #1</Link>
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

