import axios from "axios";
import * as Yup from "yup";

const context = "admin";

const versionSchema = Yup.object().required().shape({
  buildSha: Yup.string().required(),
});

const getVersion = () => axios.get(`${context}/version`).then(({ data }) => versionSchema.validate(data));

export const versionService = {
  getVersion,
};
