import React, { useContext } from "react";
import { useLocation, Outlet, Navigate } from "react-router-dom";
import { UserContext } from "../../contexts/UserContext/UserContext";

const ProtectedRoute: React.FC = () => {
  const {
    state: { loggedIn },
  } = useContext(UserContext);
  const location = useLocation();

  return loggedIn ? <Outlet /> : <Navigate to="/login" state={{ from: location }} replace />;
};

export default ProtectedRoute;
