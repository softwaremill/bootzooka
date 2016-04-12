import profileCtrl from './profileCtrl'
import profileSrv from './profileService'
import profileRouting from "./profile.routes";

export default ngModule => {
  ngModule.config(profileRouting);
  profileCtrl(ngModule);
  profileSrv(ngModule);
};
