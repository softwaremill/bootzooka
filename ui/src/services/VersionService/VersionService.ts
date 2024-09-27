import * as Yup from "yup";
import api from "api-client/apiClient";
import { Client } from "api-client/openapi.d";

const versionSchema = Yup.object().required().shape({
  buildSha: Yup.string().required(),
});

export const getVersion = () =>
  api
    .getClient<Client>()
    .then((client) => client.getAdminVersion())
    .then(({ data }) => versionSchema.validate(data));
