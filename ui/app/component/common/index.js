import header from './header';
import footer from './footer';
import sessionCtrl from './session';
import notifyCtrl from './notifications';
import versionCtrl from './version';
import bsNotif from './notifications/bsNotifications';
import bsNotifEntry from './notifications/bsNotificationEntry';
import flash from './flash';
import filters from './filters';
import form from './form';

export default ngModule => {
  header(ngModule);
  footer(ngModule);
  sessionCtrl(ngModule);
  notifyCtrl(ngModule);
  versionCtrl(ngModule);
  bsNotif(ngModule);
  bsNotifEntry(ngModule);
  flash(ngModule);
  filters(ngModule);
  form(ngModule);
};
