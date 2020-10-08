import axios from 'axios';

class PasswordService {
  static context = 'api/v1/passwordreset';

  // TODO extract to a separate service, add unit tests
  claimPasswordReset({ loginOrEmail }) {
    return axios.post(`${PasswordService.context}/forgot`, { loginOrEmail });
  }

  resetPassword({ code, password }) {
    return axios.post(`${PasswordService.context}/reset`, { code, password });
  }
}

export default PasswordService;
