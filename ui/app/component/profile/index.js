import login from './login'
import register from './register'
import profile from './profile'
import password from './password'

export default ngModule => {
  login(ngModule);
  register(ngModule);
  profile(ngModule);
  password(ngModule);
};
