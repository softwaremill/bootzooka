import profileCtrl from './profileCtrl'
import profileSrv from './profileService'
import profileRouting from "./profile.routes";

export default ngModule => {
  profileSrv(ngModule);
  profileCtrl(ngModule);
  ngModule.config(profileRouting);
};
