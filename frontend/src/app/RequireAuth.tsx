import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "@/app/AuthProvider";
import LoadingCard from "@/components/LoadingCard";
import { getAvailableRoles, getSelectedRole, setSelectedRole } from "@/lib/roleSelection";
import { useEffect, useMemo } from "react";

export default function RequireAuth() {
  const auth = useAuth();
  const location = useLocation();
  const availableRoles = useMemo(() => getAvailableRoles(), [auth.isAuthenticated]);
  const selectedRole = useMemo(() => getSelectedRole(), [auth.isAuthenticated]);

  useEffect(() => {
    if (!auth.isAuthenticated) return;
    if (availableRoles.length === 1 && !selectedRole) setSelectedRole(availableRoles[0]);
  }, [auth.isAuthenticated, availableRoles, selectedRole]);

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

  if (availableRoles.length > 1 && !selectedRole && location.pathname !== "/choose-role") {
    return <Navigate to="/choose-role" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
