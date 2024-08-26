import { AxiosRequestConfig } from "axios";
import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";
import { LoginParams, RegisterParamsPayload } from "pages";
import { ProfileDetailsParams } from "pages/Profile/components/ProfileDetails";
import { ChangePasswordDetailsParams } from "pages/Profile/components/PasswordDetails";

const apiKeySchema = Yup.object().required().shape({
  apiKey: Yup.string().required(),
});

const userDetailsSchema = Yup.object().required().shape({
  createdOn: Yup.string().required(),
  email: Yup.string().required(),
  login: Yup.string().required(),
});

export type UserDetails = Yup.InferType<typeof userDetailsSchema>;

const emptySchema = Yup.object().required().shape({});

const secureRequest = (apiKey: string): AxiosRequestConfig => ({
  headers: {
    Authorization: `Bearer ${apiKey}`,
  },
});

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

export const logout = (apiKey: string) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserLogout(null, { apiKey }, secureRequest(apiKey)))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));

export const getCurrentUser = (apiKey: string) =>
  api
    .getClient<Client>()
    .then((client) => client.getUser(null, null, secureRequest(apiKey)))
    .then(({ data }) => userDetailsSchema.validate(data));

export const changeProfileDetails = (apiKey: string, { email, login }: ProfileDetailsParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postUser(null, { email, login }, secureRequest(apiKey)))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));

export const changePassword = (apiKey: string, { currentPassword, newPassword }: ChangePasswordDetailsParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserChangepassword(null, { currentPassword, newPassword }, secureRequest(apiKey)))
    .then(({ data }) => apiKeySchema.validate(data));
