import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { adminNav } from "@/admin/adminNav";

export default function AdminHomePage() {
  const groups = Array.from(new Set(adminNav.filter((i) => i.to !== "/admin").map((i) => i.group)));
  return (
    <div className="grid gap-4">
      <Card className="overflow-hidden border bg-background/75 shadow-sm backdrop-blur">
        <CardHeader>
          <CardTitle>Overview</CardTitle>
        </CardHeader>
        <CardContent className="text-sm text-muted-foreground">
          Use the sidebar to manage resources. All admin APIs are protected by <span className="font-medium text-foreground">ROLE_ADMIN</span>.
        </CardContent>
      </Card>

      <div className="grid gap-4 md:grid-cols-2">
        {groups.map((g) => (
          <Card key={g} className="border bg-background/75 shadow-sm backdrop-blur">
            <CardHeader>
              <CardTitle className="text-base">{g}</CardTitle>
            </CardHeader>
            <CardContent className="flex flex-wrap gap-2">
              {adminNav
                .filter((i) => i.group === g && i.to !== "/admin")
                .map((i) => (
                  <a
                    key={i.to}
                    href={i.to}
                    className="inline-flex items-center gap-2 rounded-xl border bg-background/60 px-3 py-2 text-sm text-muted-foreground hover:bg-muted hover:text-foreground"
                  >
                    <i.icon className="h-4 w-4" />
                    {i.label}
                  </a>
                ))}
            </CardContent>
          </Card>
        ))}
      </div>
    </div>
  );
}

