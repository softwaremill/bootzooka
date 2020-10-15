import axios from "axios";

interface PasswordService {
  context: string;
  claimPasswordReset: (params: { loginOrEmail: string }) => Promise<any>;
  resetPassword: (params: { code: string; password: string }) => Promise<any>;
}

const passwordSerwice: PasswordService = {
  context: "api/v1/passwordreset",

  async claimPasswordReset(params) {
    const { data } = await axios.post(`${this.context}/forgot`, params);
    return data;
  },

  async resetPassword(params) {
    const { data } = await axios.post(`${this.context}/reset`, params);
    return data;
  },
};

export default passwordSerwice;
