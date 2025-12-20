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
      { path: "*", element: <NotFoundPage /> },
    ],
  },
]);
