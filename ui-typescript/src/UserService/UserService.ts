import { ChangePasswordData, LoginData, User, UserRegistrationData } from "../types/Types";
import { post, securedPost, securedGet, get } from "../Api/api";

const userUrl = '/api/v1/user';

export const registerUser = ({ login, email, password }: UserRegistrationData) => {
  return post(`${userUrl}/register`, { login, email, password });
};

export const login = ({ loginOrEmail, password }: LoginData) => {
  return post(`${userUrl}/login`, { loginOrEmail, password, apiKeyValidHours: 1 });
};

export const getCurrentUser = (apiKey: string) => {
  return securedGet(apiKey, userUrl);
};

export const changeProfileDetails = (apiKey: string, { email, login }: User) => {
  return securedPost(apiKey, userUrl, { email, login });
};

export const changePassword = (apiKey: string, { currentPassword, newPassword }: ChangePasswordData) => {
  return securedPost(apiKey, `${userUrl}/changepassword`, { currentPassword, newPassword });
};

