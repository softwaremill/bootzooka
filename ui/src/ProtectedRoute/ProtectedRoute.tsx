import React from "react";
import { Route, RouteProps } from "react-router-dom";
import { AppContext } from "../AppContext/AppContext";
import Login from "../Login/Login";

const ProtectedRoute: React.FC<RouteProps> = ({ children, ...props }) => {
  const {
    state: { user },
  } = React.useContext(AppContext);

  if (!user) return <Route {...props} component={Login} />;

  return <Route {...props}>{children}</Route>;
};

export default ProtectedRoute;
