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
//
// import kcdDirective from './directives/kcd';
// kcdDirective(ngModule);
//
// import mainCtrl from './features/main';
// mainCtrl(ngModule);
//
// import homeCtrl from './features/home';
// homeCtrl(ngModule);
//

