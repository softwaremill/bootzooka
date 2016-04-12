import passwordCtrl from './passwordResetCtrl'
import passwordSrv from './passwordResetService'
import passwordRouting from "./password.routes";

export default ngModule => {
  ngModule.config(passwordRouting);
  passwordCtrl(ngModule);
  passwordSrv(ngModule);
};
