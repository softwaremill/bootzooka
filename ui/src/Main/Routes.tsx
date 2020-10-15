import React from "react";
import { Switch, Route } from "react-router-dom";
import Welcome from "../pages/Welcome/Welcome";
import Login from "../pages/Login/Login";
import Register from "../pages/Register/Register";
import SecretMain from "../pages/SecretMain/SecretMain";
import ProfileDetails from "../pages/ProfileDetails/ProfileDetails";
import PasswordDetails from "../pages/PasswordDetails/PasswordDetails";
import RecoverLostPassword from "../pages/RecoverLostPassword/RecoverLostPassword";
import PasswordReset from "../pages/PasswordReset/PasswordReset";
import NotFound from "../pages/NotFound/NotFound";
import ProtectedRoute from "./ProtectedRoute";
import Container from "react-bootstrap/Container";
import Row from "react-bootstrap/Row";

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
      <Container>
        <Row>
          <ProfileDetails />
          <PasswordDetails />
        </Row>
      </Container>
    </ProtectedRoute>

    <Route>
      <NotFound />
    </Route>
  </Switch>
);

export default Routes;
