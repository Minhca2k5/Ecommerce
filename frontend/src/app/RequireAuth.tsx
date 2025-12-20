import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import LoadingCard from "@/components/LoadingCard";

export default function RequireAuth() {
  const auth = useAuth();
  const location = useLocation();

  if (!auth.isReady) {
    return (
      <div className="space-y-4">
        <LoadingCard />
        <LoadingCard />
      </div>
    );
  }

  if (!auth.isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
