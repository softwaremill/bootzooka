import React from "react";
import { Route, RouteProps } from "react-router-dom";
import { UserContext } from "../../contexts/UserContext/UserContext";
import Login from "../../pages/Login/Login";

const ProtectedRoute: React.FC<RouteProps> = ({ children, ...props }) => {
  const {
    state: { loggedIn },
  } = React.useContext(UserContext);

  if (!loggedIn) return <Route {...props} component={Login} />;

  return <Route {...props}>{children}</Route>;
};

export default ProtectedRoute;
