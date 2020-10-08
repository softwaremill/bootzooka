import axios, { AxiosResponse } from "axios";

interface PasswordService {
  context: string;
  claimPasswordReset: (params: { loginOrEmail: string }) => Promise<AxiosResponse<any>>;
  resetPassword: (params: { code: string; password: string }) => Promise<AxiosResponse<any>>;
}

const passwordSerwice: PasswordService = {
  context: "api/v1/passwordreset",

  // TODO extract to a separate service, add unit tests
  claimPasswordReset(params) {
    return axios.post(`${this.context}/forgot`, params);
  },

  resetPassword(params) {
    return axios.post(`${this.context}/reset`, params);
  },
};

export default passwordSerwice;
