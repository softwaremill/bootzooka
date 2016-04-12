import loginCtrl from './loginCtrl'
import loginRouting from "./login.routes";

export default ngModule => {
  ngModule.config(loginRouting);
  loginCtrl(ngModule);
};
