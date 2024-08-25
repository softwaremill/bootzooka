import OpenAPIClientAxios from "openapi-client-axios";

const apiSpecPath = "http://localhost:3000/openapi.yaml";

const api = new OpenAPIClientAxios({
  definition: apiSpecPath,
  axiosConfigDefaults: {
    baseURL: "http://localhost:8080/api/v1/",
  },
});

api.init();

export default api;
