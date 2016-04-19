import registerSessionCtrl from './userSessionCtrl'
import registerSessionSrv from './userSessionService'

export default ngModule => {
  registerSessionSrv(ngModule);
  registerSessionCtrl(ngModule);
};
