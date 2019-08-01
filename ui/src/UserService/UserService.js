import axios from 'axios';

class UserService {
  static context = 'api/v1/user';

  registerUser({ login, email, password }) {
    return axios.post(`${UserService.context}/register`, { login, email, password });
  }

  login({ loginOrEmail, password }) {
    return axios.post(`${UserService.context}/login`, { loginOrEmail, password, apiKeyValidHours: 1 });
  }

  getCurrentUser(apiKey) {
    return this._securedRequest(apiKey, {
      method: 'GET',
      url: UserService.context
    });
  }

  changeProfileDetails({ email, login }) {
    return axios.patch(`${UserService.context}`, { email, login });
  }

  changePassword(apiKey, { currentPassword, newPassword }) {
    return this._securedRequest(apiKey, {
      method: 'POST',
      url: `${UserService.context}/changepassword`,
      data: {
        currentPassword,
        newPassword
      }
    });
  }

  // TODO extract to a separate service, add unit tests
  claimPasswordReset({ login }) {
    return axios.post('passwordreset/forgot', { login });
  }

  resetPassword({ code, password }) {
    return axios.post(`passwordreset/reset/${code}`, { code, password });
  }

  _securedRequest(apiKey, config) {
    return axios.request({
      headers: {
        Authorization: `Bearer ${apiKey}`
      },
      ...config
    });
  }
}

export default UserService;
