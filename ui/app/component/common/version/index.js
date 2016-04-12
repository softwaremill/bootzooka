import registerVersionCtrl from './versionCtrl'
import registerVersionSrv from './versionService'
import registerVersion from './version'

export default ngModule => {
  registerVersion(ngModule);
  registerVersionSrv(ngModule);
  registerVersionCtrl(ngModule);
};
