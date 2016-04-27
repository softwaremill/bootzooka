import angular from 'angular'
import uirouter from 'angular-ui-router';
import sanitize from 'angular-sanitize';
import ngResource from 'angular-resource'
import ngCookies from 'angular-cookies'

import './common/css/application.css';
import {intercept, stateChangeSuccess, stateChangeStart, routing} from './app.config'
import common from './component/common';
import profile from './component/profile';
import bsTracker from './component/directives/bsHttpRequestTracker';
import homeComponent from './component/home';

const commonModule = angular.module('smlBootzooka.common', [ngCookies]);
common(commonModule);

const profileModule = angular.module('smlBootzooka.profile', [uirouter, ngResource, ngCookies, 'smlBootzooka.common']);
profile(profileModule);

const bsTrackerModule = angular.module('smlBootzooka.bsTracker', []);
bsTracker(bsTrackerModule);

const homeModule = angular.module('smlBootzooka.home', [uirouter]);
homeComponent(homeModule);

require('./component/main')(angular.module('smlBootzooka.main', [uirouter]));

const ngModule = angular.module('smlBootzooka',
    [uirouter, sanitize, 'smlBootzooka.common', 'smlBootzooka.profile', 'smlBootzooka.bsTracker', 'smlBootzooka.home', 'smlBootzooka.main'])
  .config(routing)
  .config(intercept)
  .run(stateChangeStart)
  .run(stateChangeSuccess);
