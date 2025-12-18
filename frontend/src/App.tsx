import { Button } from "@/components/ui/button";
import {
  Card,
  CardHeader,
  CardTitle,
  CardDescription,
  CardContent,
  CardFooter
} from "@/components/ui/card";


function App() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <Card className="w-full max-w-sm shadow-lg">
        <CardHeader>
          <CardTitle>Welcome to Shadcn UI</CardTitle>
          <CardDescription>
            Beautifully styled components for your React app.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <p className="mb-4">Get started by clicking the button below:</p>
          <Button className="w-full" variant="default" size="lg">
            Get Started
          </Button>
        </CardContent>
        <CardFooter>
          <span className="text-xs text-muted-foreground">Powered by shadcn/ui + Tailwind CSS</span>
        </CardFooter>
      </Card>
    </div>
  );
}

export default App
