import React from "react";
import { Routes as RouterRoutes, Route } from "react-router-dom";
import { Welcome, Login, Register, RecoverLostPassword, PasswordReset, SecretMain, Profile, NotFound } from "pages";
import { ProtectedRoute } from "./ProtectedRoute";
import { claimPasswordReset, login, register, resetPassword } from "services";

export const Routes: React.FC = () => (
  <RouterRoutes>
    <Route path="/" element={<Welcome />} />

    <Route path="/login" element={<Login onLogin={login} />} />

    <Route path="/register" element={<Register onRegisterUser={register} />} />

    <Route path="/recover-lost-password" element={<RecoverLostPassword onClaimPasswordReset={claimPasswordReset} />} />

    {/* NOTE: below path is not used anywhere, explore if we can safely remove it */}
    <Route path="/password-reset" element={<PasswordReset onPasswordReset={resetPassword} />} />

    <Route element={<ProtectedRoute />}>
      <Route path="/main" element={<SecretMain />} />
      <Route path="/profile" element={<Profile />} />
    </Route>

    <Route path="*" element={<NotFound />} />
  </RouterRoutes>
);
