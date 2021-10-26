import React from "react";
import { Route, RouteProps } from "react-router-dom";
import { UserContext } from "../../contexts/UserContext/UserContext";
import Login from "../../pages/Login/Login";
import { fold } from "fp-ts/Option";
import { pipe } from "fp-ts/pipeable";

const ProtectedRoute: React.FC<RouteProps> = ({ children, ...props }) => {
  const {
    state: { user },
  } = React.useContext(UserContext);

  return pipe(
    user,
    fold(
      () => <Route {...props} component={Login} />,
      _ => <Route {...props}>{children}</Route>
    )
  );
};

export default ProtectedRoute;
