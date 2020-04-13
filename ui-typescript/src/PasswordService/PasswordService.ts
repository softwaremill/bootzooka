import axios from 'axios';

class PasswordService {
  static context = 'api/v1/passwordreset';

  // TODO extract to a separate service, add unit tests
  static claimPasswordReset(loginOrEmail: string) {
    return axios.post(`${PasswordService.context}/forgot`, { loginOrEmail });
  }

  static resetPassword(code: string, password: string) {
    return axios.post(`${PasswordService.context}/reset`, { code, password });
  }
}

export default PasswordService;
