import React from "react";
import { Switch, Route } from "react-router-dom";
import Welcome from "../../pages/Welcome/Welcome";
import Login from "../../pages/Login/Login";
import Register from "../../pages/Register/Register";
import SecretMain from "../../pages/SecretMain/SecretMain";
import Profile from "../../pages/Profile/Profile";
import RecoverLostPassword from "../../pages/RecoverLostPassword/RecoverLostPassword";
import PasswordReset from "../../pages/PasswordReset/PasswordReset";
import NotFound from "../../pages/NotFound/NotFound";
import ProtectedRoute from "./ProtectedRoute";

const Routes: React.FC = () => (
  <Switch>
    <Route exact path="/">
      <Welcome />
    </Route>
    <Route path="/login">
      <Login />
    </Route>
    <Route path="/register">
      <Register />
    </Route>
    <Route path="/recover-lost-password">
      <RecoverLostPassword />
    </Route>
    <Route path="/password-reset">
      <PasswordReset />
    </Route>

    <ProtectedRoute path="/main">
      <SecretMain />
    </ProtectedRoute>
    <ProtectedRoute path="/profile">
      <Profile />
    </ProtectedRoute>

    <Route>
      <NotFound />
    </Route>
  </Switch>
);

export default Routes;
