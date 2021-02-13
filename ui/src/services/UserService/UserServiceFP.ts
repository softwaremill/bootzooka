import * as IO from 'io-ts';
import { fetchJson, sendJson } from "../AxiosService/AxiosService";
import * as E from 'fp-ts/Either';

const context = "api/v1/user";

const ApiKeyValidator = IO.string;

const UserDetailsValidator = IO.type({
  createdOn: IO.string,
  email: IO.string,
  login: IO.string,
});

export type UserDetails = IO.TypeOf<typeof UserDetailsValidator>;

export type ApiKey = IO.TypeOf<typeof ApiKeyValidator>;

const EmptyValidator = IO.partial({});

const registerUser = (params: { login: string, email: string, password: string }): Promise<E.Either<Error, ApiKey>> =>
  sendJson(`${context}/register`, ApiKeyValidator, params);

const login = (params: { login: string, email: string, password: string }): Promise<E.Either<Error, ApiKey>> =>
  sendJson(`${context}/login`, ApiKeyValidator, { ...params, apiKeyValidHours: 1 });

const getCurrentUser = (apiKey: string): Promise<E.Either<Error, UserDetails>> =>
  fetchJson(`${context}`, UserDetailsValidator);

const changeProfileDetails = (params: { email: string, login: string }): Promise<E.Either<Error, {}>> =>
  sendJson(`${context}`, EmptyValidator, params);

const changePassword = (params: { currentPassword: string, newPassword: string }): Promise<E.Either<Error, {}>> =>
  sendJson(`${context}/changepassword`, EmptyValidator, params);

export default {
  registerUser,
  login,
  getCurrentUser,
  changeProfileDetails,
  changePassword,
};
