import 'bootstrap/dist/css/bootstrap.css';
import './common/css/application.css';
import angular from 'angular'
import uirouter from 'angular-ui-router';
import sanitize from 'angular-sanitize';
// import routing from './app.config'

const ngModule = angular.module('smlBootzooka', [uirouter, sanitize]);//.config(routing);

import sessionCtrl from './component/session';
sessionCtrl(ngModule);

import notifyCtrl from './component/notifications';
notifyCtrl(ngModule);

import bsNotif from './component/notifications/bsNotifications';
bsNotif(ngModule);
import bsNotifEntry from './component/notifications/bsNotificationEntry';
bsNotifEntry(ngModule);
import bsTracker from './component/directives/bsHttpRequestTracker';
bsTracker(ngModule);

import versionCtrl from './component/version';
versionCtrl(ngModule);

import homeCtrl from './component/home';
homeCtrl(ngModule);

import header from './component/header';
header(ngModule);

import footer from './component/footer';
footer(ngModule);

import login from './component/login';
login(ngModule);
