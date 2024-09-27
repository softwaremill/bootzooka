import { AxiosRequestConfig } from "axios";
import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client, Components } from "api-client/openapi.d";

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

export const login = (payload: Components.Schemas.LoginIN) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserLogin(null, { ...payload, apiKeyValidHours: 1 }))
    .then(({ data }) => apiKeySchema.validate(data));

export const register = (payload: Components.Schemas.RegisterIN) =>
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

export const changeProfileDetails = (apiKey: string, payload: Components.Schemas.UpdateUserIN) =>
  api
    .getClient<Client>()
    .then((client) => client.postUser(null, payload, secureRequest(apiKey)))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));

export const changePassword = (apiKey: string, payload: Components.Schemas.ChangePasswordIN) =>
  api
    .getClient<Client>()
    .then((client) => client.postUserChangepassword(null, payload, secureRequest(apiKey)))
    .then(({ data }) => apiKeySchema.validate(data));
