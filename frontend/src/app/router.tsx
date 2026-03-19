import { Suspense, lazy, type ReactElement } from "react";
import { createBrowserRouter } from "react-router-dom";
import LoadingCard from "@/components/LoadingCard";

const AppLayout = lazy(() => import("@/app/AppLayout"));
const RequireAuth = lazy(() => import("@/app/RequireAuth"));
const RequireAdmin = lazy(() => import("@/app/RequireAdmin"));
const AdminLayout = lazy(() => import("@/admin/AdminLayout"));

const HomePage = lazy(() => import("@/pages/HomePage"));
const CategoriesPage = lazy(() => import("@/pages/CategoriesPage"));
const ProductsPage = lazy(() => import("@/pages/ProductsPage"));
const ProductDetailPage = lazy(() => import("@/pages/ProductDetailPage"));
const CategoryDetailPage = lazy(() => import("@/pages/CategoryDetailPage"));
const CartPage = lazy(() => import("@/pages/CartPage"));
const CheckoutPage = lazy(() => import("@/pages/CheckoutPage"));
const GuestOrderPage = lazy(() => import("@/pages/GuestOrderPage"));
const LoginPage = lazy(() => import("@/pages/LoginPage"));
const RegisterPage = lazy(() => import("@/pages/RegisterPage"));
const ChooseRolePage = lazy(() => import("@/pages/ChooseRolePage"));
const ProfilePage = lazy(() => import("@/pages/ProfilePage"));
const ProfileEditPage = lazy(() => import("@/pages/ProfileEditPage"));
const PasswordPage = lazy(() => import("@/pages/PasswordPage"));
const AddressesPage = lazy(() => import("@/pages/AddressesPage"));
const MyVouchersPage = lazy(() => import("@/pages/MyVouchersPage"));
const MyVoucherDetailPage = lazy(() => import("@/pages/MyVoucherDetailPage"));
const VoucherUsesPage = lazy(() => import("@/pages/VoucherUsesPage"));
const WishlistPage = lazy(() => import("@/pages/WishlistPage"));
const NotificationsPage = lazy(() => import("@/pages/NotificationsPage"));
const OrdersPage = lazy(() => import("@/pages/OrdersPage"));
const OrderDetailPage = lazy(() => import("@/pages/OrderDetailPage"));
const MomoQrPaymentPage = lazy(() => import("@/pages/MomoQrPaymentPage"));
const NotFoundPage = lazy(() => import("@/pages/NotFoundPage"));

const AdminHomePage = lazy(() => import("@/pages/admin/AdminHomePage"));
const AdminProductsPage = lazy(() => import("@/pages/admin/AdminProductsPage"));
const AdminCategoriesPage = lazy(() => import("@/pages/admin/AdminCategoriesPage"));
const AdminProductImagesPage = lazy(() => import("@/pages/admin/AdminProductImagesPage"));
const AdminOrdersPage = lazy(() => import("@/pages/admin/AdminOrdersPage"));
const AdminOrderItemsPage = lazy(() => import("@/pages/admin/AdminOrderItemsPage"));
const AdminPaymentsPage = lazy(() => import("@/pages/admin/AdminPaymentsPage"));
const AdminAnalyticsPage = lazy(() => import("@/pages/admin/AdminAnalyticsPage"));
const AdminUsersPage = lazy(() => import("@/pages/admin/AdminUsersPage"));
const AdminRolesPage = lazy(() => import("@/pages/admin/AdminRolesPage"));
const AdminAddressesPage = lazy(() => import("@/pages/admin/AdminAddressesPage"));
const AdminWarehousesPage = lazy(() => import("@/pages/admin/AdminWarehousesPage"));
const AdminInventoriesPage = lazy(() => import("@/pages/admin/AdminInventoriesPage"));
const AdminBannersPage = lazy(() => import("@/pages/admin/AdminBannersPage"));
const AdminVouchersPage = lazy(() => import("@/pages/admin/AdminVouchersPage"));
const AdminVoucherUsesPage = lazy(() => import("@/pages/admin/AdminVoucherUsesPage"));
const AdminNotificationsPage = lazy(() => import("@/pages/admin/AdminNotificationsPage"));
const AdminAuditLogsPage = lazy(() => import("@/pages/admin/AdminAuditLogsPage"));
const AdminReviewsPage = lazy(() => import("@/pages/admin/AdminReviewsPage"));
const AdminProfilePage = lazy(() => import("@/pages/admin/AdminProfilePage"));
const AdminProfileEditPage = lazy(() => import("@/pages/admin/AdminProfileEditPage"));
const AdminPasswordPage = lazy(() => import("@/pages/admin/AdminPasswordPage"));

function lazyElement(element: ReactElement) {
  return (
    <Suspense fallback={<LoadingCard />}>
      {element}
    </Suspense>
  );
}

export const router = createBrowserRouter([
  {
    path: "/",
    element: lazyElement(<AppLayout />),
    children: [
      { index: true, element: lazyElement(<HomePage />) },
      { path: "categories", element: lazyElement(<CategoriesPage />) },
      { path: "products", element: lazyElement(<ProductsPage />) },
      { path: "products/slug/:slug", element: lazyElement(<ProductDetailPage />) },
      { path: "products/:productId", element: lazyElement(<ProductDetailPage />) },
      { path: "categories/slug/:slug", element: lazyElement(<CategoryDetailPage />) },
      { path: "categories/:categoryId", element: lazyElement(<CategoryDetailPage />) },
      { path: "cart", element: lazyElement(<CartPage />) },
      { path: "checkout", element: lazyElement(<CheckoutPage />) },
      { path: "guest/orders/:orderId", element: lazyElement(<GuestOrderPage />) },
      { path: "login", element: lazyElement(<LoginPage />) },
      { path: "register", element: lazyElement(<RegisterPage />) },
      {
        element: lazyElement(<RequireAuth />),
        children: [
          { path: "choose-role", element: lazyElement(<ChooseRolePage />) },
          { path: "me", element: lazyElement(<ProfilePage />) },
          { path: "me/edit", element: lazyElement(<ProfileEditPage />) },
          { path: "me/password", element: lazyElement(<PasswordPage />) },
          { path: "me/addresses", element: lazyElement(<AddressesPage />) },
          { path: "me/vouchers", element: lazyElement(<MyVouchersPage />) },
          { path: "me/vouchers/:voucherId", element: lazyElement(<MyVoucherDetailPage />) },
          { path: "me/voucher-uses", element: lazyElement(<VoucherUsesPage />) },
          { path: "me/wishlist", element: lazyElement(<WishlistPage />) },
          { path: "notifications", element: lazyElement(<NotificationsPage />) },
          { path: "orders", element: lazyElement(<OrdersPage />) },
          { path: "orders/:orderId", element: lazyElement(<OrderDetailPage />) },
          { path: "orders/:orderId/momo-qr", element: lazyElement(<MomoQrPaymentPage />) },
        ],
      },
      {
        path: "admin",
        element: lazyElement(<RequireAdmin />),
        children: [
          {
            element: lazyElement(<AdminLayout />),
            children: [
              { index: true, element: lazyElement(<AdminHomePage />) },
              { path: "products", element: lazyElement(<AdminProductsPage />) },
              { path: "categories", element: lazyElement(<AdminCategoriesPage />) },
              { path: "product-images", element: lazyElement(<AdminProductImagesPage />) },
              { path: "orders", element: lazyElement(<AdminOrdersPage />) },
              { path: "order-items", element: lazyElement(<AdminOrderItemsPage />) },
              { path: "payments", element: lazyElement(<AdminPaymentsPage />) },
              { path: "analytics", element: lazyElement(<AdminAnalyticsPage />) },
              { path: "users", element: lazyElement(<AdminUsersPage />) },
              { path: "roles", element: lazyElement(<AdminRolesPage />) },
              { path: "addresses", element: lazyElement(<AdminAddressesPage />) },
              { path: "warehouses", element: lazyElement(<AdminWarehousesPage />) },
              { path: "inventories", element: lazyElement(<AdminInventoriesPage />) },
              { path: "banners", element: lazyElement(<AdminBannersPage />) },
              { path: "vouchers", element: lazyElement(<AdminVouchersPage />) },
              { path: "voucher-uses", element: lazyElement(<AdminVoucherUsesPage />) },
              { path: "notifications", element: lazyElement(<AdminNotificationsPage />) },
              { path: "audit-logs", element: lazyElement(<AdminAuditLogsPage />) },
              { path: "reviews", element: lazyElement(<AdminReviewsPage />) },
              { path: "profile", element: lazyElement(<AdminProfilePage />) },
              { path: "profile/edit", element: lazyElement(<AdminProfileEditPage />) },
              { path: "profile/password", element: lazyElement(<AdminPasswordPage />) },
            ],
          },
        ],
      },
      { path: "*", element: lazyElement(<NotFoundPage />) },
    ],
  },
]);
