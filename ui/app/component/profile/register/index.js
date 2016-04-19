import registerCtrl from './registerCtrl'
import registerSrv from './registerService'
import registerRouting from "./register.routes";

export default ngModule => {
  ngModule.config(registerRouting);
  registerCtrl(ngModule);
  registerSrv(ngModule);
};
