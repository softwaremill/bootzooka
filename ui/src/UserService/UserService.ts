import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

interface UserService {
  context: string;
  registerUser: (params: { login: string; email: string; password: string }) => Promise<AxiosResponse<any>>;
  login: (params: { loginOrEmail: string; password: string }) => Promise<AxiosResponse<any>>;
  getCurrentUser: (apiKey: string) => Promise<AxiosResponse<any>>;
  changeProfileDetails: (apiKey: string, params: { email: string; login: string }) => Promise<AxiosResponse<any>>;
  changePassword: (
    apiKey: string,
    params: { currentPassword: string; newPassword: string }
  ) => Promise<AxiosResponse<any>>;
  _securedRequest: (apiKey: string, config: AxiosRequestConfig) => Promise<AxiosResponse<any>>;
}

const userService: UserService = {
  context: "api/v1/user",

  registerUser(params) {
    return axios.post(`${this.context}/register`, params);
  },

  login(params) {
    return axios.post(`${this.context}/login`, { ...params, apiKeyValidHours: 1 });
  },

  getCurrentUser(apiKey) {
    return this._securedRequest(apiKey, {
      method: "GET",
      url: this.context,
    });
  },

  changeProfileDetails(apiKey, params) {
    return this._securedRequest(apiKey, {
      method: "POST",
      url: this.context,
      data: params,
    });
  },

  changePassword(apiKey, params) {
    return this._securedRequest(apiKey, {
      method: "POST",
      url: `${this.context}/changepassword`,
      data: params,
    });
  },

  _securedRequest(apiKey, config) {
    return axios.request({
      headers: {
        Authorization: `Bearer ${apiKey}`,
      },
      ...config,
    });
  },
};

export default userService;
