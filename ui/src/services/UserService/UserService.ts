import axios, { AxiosRequestConfig } from "axios";
import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";
import { LoginParams, RegisterParamsPayload } from "pages";

const context = "api/v1/user";

const apiKeySchema = Yup.object().required().shape({
  apiKey: Yup.string().required(),
});

const userDetailsSchema = Yup.object().required().shape({
  createdOn: Yup.string().required(),
  email: Yup.string().required(),
  login: Yup.string().required(),
});

const emptySchema = Yup.object().required().shape({});

export const login = (params: LoginParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserLogin(null, { ...params, apiKeyValidHours: 1 }))
    .then(({ data }) => apiKeySchema.validate(data));

export const register = (payload: RegisterParamsPayload) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserRegister(null, payload))
    .then(({ data }) => apiKeySchema.validate(data));

const logout = (apiKey: string | null) =>
  _securedRequest(apiKey, {
    method: "POST",
    url: `${context}/logout`,
    data: { apiKey },
  }).then(({ data }) => emptySchema.validate(data));

const getCurrentUser = (apiKey: string | null) =>
  _securedRequest(apiKey, {
    method: "GET",
    url: context,
  }).then(({ data }) => userDetailsSchema.validate(data));

const changeProfileDetails = (apiKey: string | null, params: { email: string; login: string }) =>
  _securedRequest(apiKey, {
    method: "POST",
    url: context,
    data: params,
  }).then(({ data }) => emptySchema.validate(data));

const changePassword = (apiKey: string | null, params: { currentPassword: string; newPassword: string }) =>
  _securedRequest(apiKey, {
    method: "POST",
    url: `${context}/changepassword`,
    data: params,
  }).then(({ data }) => apiKeySchema.validate(data));

const _securedRequest = (apiKey: string | null, config: AxiosRequestConfig) =>
  axios.request({
    headers: {
      Authorization: `Bearer ${apiKey}`,
    },
    ...config,
  });

export const userService = {
  logout,
  getCurrentUser,
  changeProfileDetails,
  changePassword,
};
