import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

type ApiKey = string | null;

interface UserService {
  context: string;
  registerUser: (params: { login: string; email: string; password: string }) => Promise<any>;
  login: (params: { loginOrEmail: string; password: string }) => Promise<any>;
  getCurrentUser: (apiKey: ApiKey) => Promise<any>;
  changeProfileDetails: (apiKey: ApiKey, params: { email: string; login: string }) => Promise<any>;
  changePassword: (apiKey: ApiKey, params: { currentPassword: string; newPassword: string }) => Promise<any>;
  _securedRequest: (apiKey: ApiKey, config: AxiosRequestConfig) => Promise<AxiosResponse<any>>;
}

const userService: UserService = {
  context: "api/v1/user",

  async registerUser(params) {
    const { data } = await axios.post(`${this.context}/register`, params);
    return data;
  },

  async login(params) {
    const { data } = await axios.post(`${this.context}/login`, { ...params, apiKeyValidHours: 1 });
    return data;
  },

  async getCurrentUser(apiKey) {
    const { data } = await this._securedRequest(apiKey, {
      method: "GET",
      url: this.context,
    });
    return data;
  },

  async changeProfileDetails(apiKey, params) {
    const { data } = await this._securedRequest(apiKey, {
      method: "POST",
      url: this.context,
      data: params,
    });
    return data;
  },

  async changePassword(apiKey, params) {
    const { data } = await this._securedRequest(apiKey, {
      method: "POST",
      url: `${this.context}/changepassword`,
      data: params,
    });
    return data;
  },

  _securedRequest(apiKey, config) {
    if (!apiKey) throw new Error("Api Key not provided");

    return axios.request({
      headers: {
        Authorization: `Bearer ${apiKey}`,
      },
      ...config,
    });
  },
};

export default userService;
