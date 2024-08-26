import axios from "axios";
import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";
import { RecoverLostPasswordParams } from "pages";

const context = "api/v1/passwordreset";

const emptySchema = Yup.object().required().shape({});

export const claimPasswordReset = (params: RecoverLostPasswordParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postPasswordresetForgot(null, params))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));

const resetPassword = (params: { code: string; password: string }) =>
  axios.post(`${context}/reset`, params).then(({ data }) => emptySchema.validate(data));

export const passwordService = {
  resetPassword,
};
