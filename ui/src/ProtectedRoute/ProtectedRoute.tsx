import React from "react";
import { Route, RouteProps } from "react-router-dom";
import { AppContext } from "../AppContext/AppContext";
import Login from "../Login/Login";

const ProtectedRoute: React.FC<RouteProps> = ({ children, ...props }) => {
  const {
    state: { loggedIn },
  } = React.useContext(AppContext);

  if (!loggedIn) return <Route {...props} component={Login} />;

  return <Route {...props}>{children}</Route>;
};

export default ProtectedRoute;
