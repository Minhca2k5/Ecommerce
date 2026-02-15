import type { LucideIcon } from "lucide-react";
import {
  LayoutDashboard,
  Package,
  Tags,
  Image as ImageIcon,
  ShoppingBag,
  CreditCard,
  Users,
  Shield,
  MapPin,
  Warehouse,
  Boxes,
  Flag,
  TicketPercent,
  Bell,
  Star,
  ScrollText,
  BarChart3,
} from "lucide-react";

export type AdminNavItem = {
  to: string;
  label: string;
  icon: LucideIcon;
  group:
    | "Catalog"
    | "Sales"
    | "Users"
    | "Inventory"
    | "Marketing"
    | "System";
};

export const adminNav: AdminNavItem[] = [
  { to: "/admin", label: "Overview", icon: LayoutDashboard, group: "System" },

  { to: "/admin/products", label: "Products", icon: Package, group: "Catalog" },
  { to: "/admin/categories", label: "Categories", icon: Tags, group: "Catalog" },
  { to: "/admin/product-images", label: "Product images", icon: ImageIcon, group: "Catalog" },

  { to: "/admin/orders", label: "Orders", icon: ShoppingBag, group: "Sales" },
  { to: "/admin/payments", label: "Payments", icon: CreditCard, group: "Sales" },
  { to: "/admin/analytics", label: "Analytics", icon: BarChart3, group: "Sales" },

  { to: "/admin/users", label: "Users", icon: Users, group: "Users" },
  { to: "/admin/roles", label: "Roles", icon: Shield, group: "Users" },
  { to: "/admin/addresses", label: "Addresses", icon: MapPin, group: "Users" },

  { to: "/admin/warehouses", label: "Warehouses", icon: Warehouse, group: "Inventory" },
  { to: "/admin/inventories", label: "Inventories", icon: Boxes, group: "Inventory" },

  { to: "/admin/banners", label: "Banners", icon: Flag, group: "Marketing" },
  { to: "/admin/vouchers", label: "Vouchers", icon: TicketPercent, group: "Marketing" },
  { to: "/admin/voucher-uses", label: "Voucher uses", icon: TicketPercent, group: "Marketing" },

  { to: "/admin/notifications", label: "Notifications", icon: Bell, group: "System" },
  { to: "/admin/audit-logs", label: "Audit logs", icon: ScrollText, group: "System" },
  { to: "/admin/reviews", label: "Reviews", icon: Star, group: "System" },
];
