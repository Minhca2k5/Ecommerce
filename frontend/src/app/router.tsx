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
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);
