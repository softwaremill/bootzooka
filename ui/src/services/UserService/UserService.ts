import { AxiosRequestConfig } from "axios";
import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client, Components } from "api-client/openapi.d";

const apiKeySchema = Yup.object().shape({
  apiKey: Yup.string().required(),
});

const userDetailsSchema = Yup.object().shape({
  createdOn: Yup.string().required(),
  email: Yup.string().required(),
  login: Yup.string().required(),
});

export type UserDetails = Yup.InferType<typeof userDetailsSchema>;

const emptySchema = Yup.object().shape({});

const secureRequest = (apiKey: string): AxiosRequestConfig => ({
  headers: {
    Authorization: `Bearer ${apiKey}`,
  },
});

const handleRequest = async <T>(
  requestFn: (client: Client) => Promise<T>,
  schema: Yup.ObjectSchema<any>
) => {
  try {
    const client = await api.getClient<Client>();
    const response = await requestFn(client);

    const responseData = (response as any)?.data ?? response;
    return schema.validate(responseData);
  } catch (error) {
    throw error;
  }
};

const postRequest = <T>(
  endpointFn: (client: Client, data: any, config?: AxiosRequestConfig) => Promise<T>,
  apiKey: string | null,
  payload: any,
  schema: Yup.ObjectSchema<any>
) => handleRequest((client) => endpointFn(client, payload, apiKey ? secureRequest(apiKey) : undefined), schema);

const getRequest = <T>(
  endpointFn: (client: Client, config?: AxiosRequestConfig) => Promise<T>,
  apiKey: string,
  schema: Yup.ObjectSchema<any>
) => handleRequest((client) => endpointFn(client, secureRequest(apiKey)), schema);

export const login = (payload: Components.Schemas.LoginIN) =>
  postRequest((client, data) => client.postUserLogin(null, { ...data, apiKeyValidHours: 1 }), null, payload, apiKeySchema);

export const register = (payload: Components.Schemas.RegisterIN) =>
  postRequest((client, data) => client.postUserRegister(null, data), null, payload, apiKeySchema);

export const logout = (apiKey: string) =>
  postRequest((client, data) => client.postUserLogout(null, data), apiKey, { apiKey }, emptySchema);

export const getCurrentUser = (apiKey: string) =>
  getRequest((client) => client.getUser(null, null), apiKey, userDetailsSchema);

export const changeProfileDetails = (apiKey: string, payload: Components.Schemas.UpdateUserIN) =>
  postRequest((client, data) => client.postUser(null, data), apiKey, payload, emptySchema);

export const changePassword = (apiKey: string, payload: Components.Schemas.ChangePasswordIN) =>
  postRequest((client, data) => client.postUserChangepassword(null, data), apiKey, payload, apiKeySchema);
