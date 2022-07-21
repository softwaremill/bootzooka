import React from "react";
import { useLocation, Outlet, Navigate } from "react-router-dom";
import { UserContext } from "contexts";

export const ProtectedRoute: React.FC = () => {
  const {
    state: { loggedIn },
  } = React.useContext(UserContext);
  const location = useLocation();

  return loggedIn ? <Outlet /> : <Navigate to="/login" state={{ from: location }} replace />;
};
