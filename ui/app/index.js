import 'bootstrap/dist/css/bootstrap.css';
import './common/css/application.css';
import angular from 'angular'
import uirouter from 'angular-ui-router';
import sanitize from 'angular-sanitize';
import {intercept, stateChangeSuccess, stateChangeStart, routing} from './app.config'
import ngResource from 'angular-resource'
import ngCookies from 'angular-cookies'

const commonModule = angular.module('smlBootzooka.common', [ngCookies]);
import common from './component/common';
common(commonModule);

const profileModule = angular.module('smlBootzooka.profile', [uirouter, ngResource, ngCookies, 'smlBootzooka.common']);
import profile from './component/profile';
profile(profileModule);

const bsTrackerModule = angular.module('smlBootzooka.bsTracker', []);
import bsTracker from './component/directives/bsHttpRequestTracker';
bsTracker(bsTrackerModule);

const homeModule = angular.module('smlBootzooka.home', [uirouter]);
import homeCtrl from './component/home';
homeCtrl(homeModule);

const ngModule = angular.module('smlBootzooka', 
    [uirouter, sanitize, 'smlBootzooka.common', 'smlBootzooka.profile', 'smlBootzooka.bsTracker', 'smlBootzooka.home'])
  .config(routing)
  .config(intercept)
  .run(stateChangeStart)
  .run(stateChangeSuccess);
