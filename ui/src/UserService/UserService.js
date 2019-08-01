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

  changeProfileDetails(apiKey, { email, login }) {
    return this._securedRequest(apiKey, {
      method: 'POST',
      url: UserService.context,
      data: {
        email,
        login
      }
    });
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
