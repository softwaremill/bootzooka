import axios, { AxiosRequestConfig, AxiosResponse } from "axios";

interface UserService {
  context: string;
  registerUser: (params: { login: string; email: string; password: string }) => Promise<any>;
  login: (params: { loginOrEmail: string; password: string }) => Promise<any>;
  getCurrentUser: (apiKey: string) => Promise<any>;
  changeProfileDetails: (apiKey: string, params: { email: string; login: string }) => Promise<any>;
  changePassword: (apiKey: string, params: { currentPassword: string; newPassword: string }) => Promise<any>;
  _securedRequest: (apiKey: string, config: AxiosRequestConfig) => Promise<AxiosResponse<any>>;
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
    return axios.request({
      headers: {
        Authorization: `Bearer ${apiKey}`,
      },
      ...config,
    });
  },
};

export default userService;
