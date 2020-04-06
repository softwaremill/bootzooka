import axios, { AxiosRequestConfig } from 'axios';
import { ChangePasswordData, LoginData, UserRegistrationData } from "../types/Types";

const userUrl = 'api/v1/user';

const registerUser = ({ login, email, password }: UserRegistrationData) => {
  return axios.post(`${userUrl}/register`, { login, email, password });
};

const login = ({ loginOrEmail, password }: LoginData) => {
  return axios.post(`${userUrl}/login`, { loginOrEmail, password, apiKeyValidHours: 1 });
};

const getCurrentUser = (apiKey: string) => {
  return _securedRequest(apiKey, {
    method: 'GET',
    url: userUrl
  });
};

const changeProfileDetails = (apiKey: string, { email, login }: { email: string, login: string }) => {
  return _securedRequest(apiKey, {
    method: 'POST',
    url: userUrl,
    data: {
      email,
      login
    }
  });
};

const changePassword = (apiKey: string, { currentPassword, newPassword }: ChangePasswordData) => {
  return _securedRequest(apiKey, {
    method: 'POST',
    url: `${userUrl}/changepassword`,
    data: {
      currentPassword,
      newPassword
    }
  });
};

const _securedRequest = (apiKey: string, config?: AxiosRequestConfig) => {
  return axios.request({
    headers: {
      Authorization: `Bearer ${apiKey}`
    },
    ...config
  });
};

export default UserService;
