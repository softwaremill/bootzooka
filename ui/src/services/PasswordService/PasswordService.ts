import axios from "axios";
import * as Yup from "yup";

const context = "api/v1/passwordreset";

const emptySchema = Yup.object().required().shape({});

const claimPasswordReset = (params: { loginOrEmail: string }) =>
  axios.post(`${context}/forgot`, params).then(({ data }) => emptySchema.validate(data));

const resetPassword = (params: { code: string; password: string }) =>
  axios.post(`${context}/reset`, params).then(({ data }) => emptySchema.validate(data));

export default { claimPasswordReset, resetPassword };
