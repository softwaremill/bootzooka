import React from "react";
import { Routes as RouterRoutes, Route } from "react-router-dom";
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
  <RouterRoutes>
    <Route path="/" element={<Welcome />} />

    <Route path="/login" element={<Login />} />

    <Route path="/register" element={<Register />} />

    <Route path="/recover-lost-password" element={<RecoverLostPassword />} />

    <Route path="/password-reset" element={<PasswordReset />} />

    <Route element={<ProtectedRoute />}>
      <Route path="/main" element={<SecretMain />} />
      <Route path="/profile" element={<Profile />} />
    </Route>

    <Route path="*" element={<NotFound />} />
  </RouterRoutes>
);

export default Routes;
