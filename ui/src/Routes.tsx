import React from "react";
import { Switch, Route } from "react-router-dom";
import Welcome from "./Welcome/Welcome";
import Login from "./Login/Login";
import Register from "./Register/Register";
import SecretMain from "./SecretMain/SecretMain";
import ProfileDetails from "./ProfileDetails/ProfileDetails";
import PasswordDetails from "./PasswordDetails/PasswordDetails";
import RecoverLostPassword from "./RecoverLostPassword/RecoverLostPassword";
import PasswordReset from "./PasswordReset/PasswordReset";
import NotFound from "./NotFound/NotFound";
import ProtectedRoute from "./ProtectedRoute/ProtectedRoute";
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
