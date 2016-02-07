'use strict';

angular.module('smlBootzooka.session').factory('UserSessionService', ($http, $rootScope, $log, $window) => {

  let loggedUser = null;
  let target = null;

  let loggedUserPromise = $http.get('api/users').then(response => {
    loggedUser = response.data;
    return loggedUser;
  });

  let userSessionService = {};

  userSessionService.getLoggedUser = () => loggedUser;

  userSessionService.getLoggedUserPromise = () => loggedUserPromise;

  userSessionService.isLogged = () => angular.isObject(loggedUser);

  userSessionService.isNotLogged = () => !userSessionService.isLogged();

  userSessionService.login = user => $http.post('api/users', angular.toJson(user)).then(response => {
    loggedUser = response.data;
    return response.data;
  });

  userSessionService.resetLoggedUser = () => loggedUser = null;

  userSessionService.logout = () => $http.get('api/users/logout').then(() => {
    userSessionService.resetLoggedUser();
    //this line reloads page and we are sure that there are not leftovers of logged user anywhere.
    $window.location = '/';
  });

  userSessionService.getLoggedUserName = () => {
    if (loggedUser) {
      return loggedUser.login;
    } else {
      return '';
    }
  };

  userSessionService.saveTarget = (targetState, targetParams) =>
    target = {targetState: targetState, targetParams: targetParams};

  userSessionService.loadTarget = () => {
    let result = target;
    target = null;
    return result;
  };

  userSessionService.updateLogin = login => {
    if (loggedUser) {
      loggedUser.login = login;
    } else {
      $log.warn('Trying to updated login but user is null');
    }
  };

  userSessionService.updateEmail = email => {
    if (loggedUser) {
      loggedUser.email = email;
    } else {
      $log.warn('Trying to updated email but user is null');
    }
  };

  $rootScope.isLogged = userSessionService.isLogged;
  $rootScope.isNotLogged = userSessionService.isNotLogged;

  return userSessionService;
});
