import axios from "axios";

interface VersionService {
  context: string;
  getVersion: () => Promise<any>;
}

const versionService: VersionService = {
  context: "admin",

  async getVersion() {
    const { data } = await axios.get(`${this.context}/version`);
    return data;
  },
};

export default versionService;
