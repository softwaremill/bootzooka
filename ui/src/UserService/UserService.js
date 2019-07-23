import axios from 'axios';

class UserService {
  registerUser({ login, email, password }) {
    return axios.post('api/users/register', { login, email, password });
  }

  claimPasswordReset({ login }) {
    return axios.post('api/passwordreset', { login });
  }

  login({ login, password, rememberMe }) {
    return axios.post('api/users', { login, password, rememberMe });
  }

  logout() {
    return axios.get('api/users/logout');
  }

  getCurrentUser() {
    return axios.get('api/users');
  }

  changeProfileDetails({ email, login }) {
    return axios.patch('api/users', { email, login });
  }

  changePassword({ currentPassword, newPassword }) {
    return axios.post('api/users/changepassword', { currentPassword, newPassword });
  }

  resetPassword({ code, password }) {
    return axios.post(`api/passwordreset/${code}`, { code, password });
  }
}

export default UserService;
