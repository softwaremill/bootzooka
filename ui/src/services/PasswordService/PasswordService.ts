import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";
import { PasswordResetRequestParams, RecoverLostPasswordParams } from "pages";

const emptySchema = Yup.object().required().shape({});

export const claimPasswordReset = (params: RecoverLostPasswordParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postPasswordresetForgot(null, params))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));

export const resetPassword = (params: PasswordResetRequestParams) =>
  api
    .getClient<Client>()
    .then((client) => client.postPasswordresetReset(null, params))
    .then(({ data }) => emptySchema.validate(data).then(() => undefined));
