import axios from 'axios';

class VersionService {
  getVersion() {
    return axios.get('api/version');
  }
}

export default VersionService;
