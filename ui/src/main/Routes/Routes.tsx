import React from "react";
import { Routes as RouterRoutes, Route } from "react-router-dom";
import {
  Welcome,
  Login,
  Register,
  RegisterParamsPayload,
  RecoverLostPassword,
  PasswordReset,
  SecretMain,
  Profile,
  NotFound,
  LoginParams,
} from "pages";
import { ProtectedRoute } from "./ProtectedRoute";
import { apiKeySchema } from "services";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";

const onRegisterUser = (payload: RegisterParamsPayload) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserRegister(null, payload))
    .then(({ data }) => apiKeySchema.validate(data));

const onLogin = (params: LoginParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserLogin(null, { ...params, apiKeyValidHours: 1 }))
    .then(({ data }) => apiKeySchema.validate(data));

export const Routes: React.FC = () => (
  <RouterRoutes>
    <Route path="/" element={<Welcome />} />

    <Route path="/login" element={<Login onLogin={onLogin} />} />

    <Route path="/register" element={<Register onRegisterUser={onRegisterUser} />} />

    <Route path="/recover-lost-password" element={<RecoverLostPassword />} />

    {/* NOTE: below path is not used anywhere, explore if we can safely remove it */}
    <Route path="/password-reset" element={<PasswordReset />} />

    <Route element={<ProtectedRoute />}>
      <Route path="/main" element={<SecretMain />} />
      <Route path="/profile" element={<Profile />} />
    </Route>

    <Route path="*" element={<NotFound />} />
  </RouterRoutes>
);
