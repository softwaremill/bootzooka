import axios from 'axios';

class VersionService {
  static context = 'admin';

  getVersion() {
    return axios.get(`${VersionService.context}/version`);
  }
}

export default VersionService;
