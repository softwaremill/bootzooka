import registerNotifyCtrl from './mainNotificationsCtrl'
import registerNotifySrv from './notificationsService'

export default ngModule => {
  registerNotifySrv(ngModule);
  registerNotifyCtrl(ngModule);
};
