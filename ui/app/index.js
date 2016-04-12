import 'bootstrap/dist/css/bootstrap.css';
import './common/css/application.css';
import angular from 'angular'
import uirouter from 'angular-ui-router';
import sanitize from 'angular-sanitize';
import {intercept, stateChangeSuccess, stateChangeStart, routing} from './app.config'
import ngResource from 'angular-resource'
import ngCookies from 'angular-cookies'


const ngModule = angular.module('smlBootzooka', [uirouter, sanitize, ngResource, ngCookies])
  .config(routing)
  .config(intercept)
  .run(stateChangeStart)
  .run(stateChangeSuccess);

import bsTracker from './component/directives/bsHttpRequestTracker';
bsTracker(ngModule);

import common from './component/common';
common(ngModule);

import homeCtrl from './component/home';
homeCtrl(ngModule);

import profile from './component/profile';
profile(ngModule);
