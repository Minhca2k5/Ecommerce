import { createBrowserRouter } from "react-router-dom";
import AppLayout from "./AppLayout";
import CategoryDetailPage from "@/pages/CategoryDetailPage";
import CategoriesPage from "@/pages/CategoriesPage";
import HomePage from "@/pages/HomePage";
import LoginPage from "@/pages/LoginPage";
import RegisterPage from "@/pages/RegisterPage";
import NotFoundPage from "@/pages/NotFoundPage";
import ProductDetailPage from "@/pages/ProductDetailPage";
import ProductsPage from "@/pages/ProductsPage";
import RequireAuth from "@/app/RequireAuth";
import ProfilePage from "@/pages/ProfilePage";
import ProfileEditPage from "@/pages/ProfileEditPage";
import PasswordPage from "@/pages/PasswordPage";
import AddressesPage from "@/pages/AddressesPage";
import CartPage from "@/pages/CartPage";
import CheckoutPage from "@/pages/CheckoutPage";
import OrdersPage from "@/pages/OrdersPage";
import OrderDetailPage from "@/pages/OrderDetailPage";
import NotificationsPage from "@/pages/NotificationsPage";
import VoucherUsesPage from "@/pages/VoucherUsesPage";
import WishlistPage from "@/pages/WishlistPage";
import SearchLogsPage from "@/pages/SearchLogsPage";
import RequireAdmin from "@/app/RequireAdmin";
import AdminLayout from "@/admin/AdminLayout";
import AdminHomePage from "@/pages/admin/AdminHomePage";
import AdminProductsPage from "@/pages/admin/AdminProductsPage";
import AdminCategoriesPage from "@/pages/admin/AdminCategoriesPage";
import AdminProductImagesPage from "@/pages/admin/AdminProductImagesPage";
import AdminOrdersPage from "@/pages/admin/AdminOrdersPage";
import AdminOrderItemsPage from "@/pages/admin/AdminOrderItemsPage";
import AdminPaymentsPage from "@/pages/admin/AdminPaymentsPage";
import AdminUsersPage from "@/pages/admin/AdminUsersPage";
import AdminRolesPage from "@/pages/admin/AdminRolesPage";
import AdminAddressesPage from "@/pages/admin/AdminAddressesPage";
import AdminWarehousesPage from "@/pages/admin/AdminWarehousesPage";
import AdminInventoriesPage from "@/pages/admin/AdminInventoriesPage";
import AdminBannersPage from "@/pages/admin/AdminBannersPage";
import AdminVouchersPage from "@/pages/admin/AdminVouchersPage";
import AdminVoucherUsesPage from "@/pages/admin/AdminVoucherUsesPage";
import AdminNotificationsPage from "@/pages/admin/AdminNotificationsPage";
import AdminReviewsPage from "@/pages/admin/AdminReviewsPage";
import ChooseRolePage from "@/pages/ChooseRolePage";
import AdminProfilePage from "@/pages/admin/AdminProfilePage";
import AdminProfileEditPage from "@/pages/admin/AdminProfileEditPage";
import AdminPasswordPage from "@/pages/admin/AdminPasswordPage";

export const router = createBrowserRouter([
  {
    path: "/",
    element: <AppLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: "categories", element: <CategoriesPage /> },
      { path: "products", element: <ProductsPage /> },
      { path: "products/slug/:slug", element: <ProductDetailPage /> },
      { path: "products/:productId", element: <ProductDetailPage /> },
      { path: "categories/slug/:slug", element: <CategoryDetailPage /> },
      { path: "categories/:categoryId", element: <CategoryDetailPage /> },
      { path: "login", element: <LoginPage /> },
      { path: "register", element: <RegisterPage /> },
      {
        element: <RequireAuth />,
        children: [
          { path: "choose-role", element: <ChooseRolePage /> },
          { path: "me", element: <ProfilePage /> },
          { path: "me/edit", element: <ProfileEditPage /> },
          { path: "me/password", element: <PasswordPage /> },
          { path: "me/addresses", element: <AddressesPage /> },
          { path: "me/voucher-uses", element: <VoucherUsesPage /> },
          { path: "me/wishlist", element: <WishlistPage /> },
          { path: "me/search-logs", element: <SearchLogsPage /> },
          { path: "notifications", element: <NotificationsPage /> },
          { path: "cart", element: <CartPage /> },
          { path: "checkout", element: <CheckoutPage /> },
          { path: "orders", element: <OrdersPage /> },
          { path: "orders/:orderId", element: <OrderDetailPage /> },
        ],
      },
      {
        path: "admin",
        element: <RequireAdmin />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              { index: true, element: <AdminHomePage /> },
              { path: "products", element: <AdminProductsPage /> },
              { path: "categories", element: <AdminCategoriesPage /> },
              { path: "product-images", element: <AdminProductImagesPage /> },
              { path: "orders", element: <AdminOrdersPage /> },
              { path: "order-items", element: <AdminOrderItemsPage /> },
              { path: "payments", element: <AdminPaymentsPage /> },
              { path: "users", element: <AdminUsersPage /> },
              { path: "roles", element: <AdminRolesPage /> },
              { path: "addresses", element: <AdminAddressesPage /> },
              { path: "warehouses", element: <AdminWarehousesPage /> },
              { path: "inventories", element: <AdminInventoriesPage /> },
              { path: "banners", element: <AdminBannersPage /> },
              { path: "vouchers", element: <AdminVouchersPage /> },
              { path: "voucher-uses", element: <AdminVoucherUsesPage /> },
              { path: "notifications", element: <AdminNotificationsPage /> },
              { path: "reviews", element: <AdminReviewsPage /> },
              { path: "profile", element: <AdminProfilePage /> },
              { path: "profile/edit", element: <AdminProfileEditPage /> },
              { path: "profile/password", element: <AdminPasswordPage /> },
            ],
          },
        ],
      },
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);
