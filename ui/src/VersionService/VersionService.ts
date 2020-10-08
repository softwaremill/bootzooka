import axios, { AxiosResponse } from "axios";

interface VersionService {
  context: string;
  getVersion: () => Promise<AxiosResponse<any>>;
}

const versionService: VersionService = {
  context: "admin",
  getVersion() {
    return axios.get(`${this.context}/version`);
  },
};

export default versionService;
