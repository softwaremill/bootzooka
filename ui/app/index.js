import 'bootstrap/dist/css/bootstrap.css';
import './common/css/application.css';
import angular from 'angular'
import uirouter from 'angular-ui-router';
// import routing from './app.config'

const ngModule = angular.module('smlBootzooka', [uirouter]);//.config(routing);

import header from './component/header';
header(ngModule);

import footer from './component/footer';
footer(ngModule);

import sessionCtrl from './component/session';
sessionCtrl(ngModule);

import notifyCtrl from './component/notifications';
notifyCtrl(ngModule);

import versionCtrl from './component/version';
versionCtrl(ngModule);

import homeCtrl from './component/home';
homeCtrl(ngModule);


