'use strict';

angular.module('smlBootzooka.session').factory('UserSessionService', function ($http, $rootScope, $log, $window) {

    var loggedUser = null;
    var target = null;

    var loggedUserPromise = $http.get('api/users').then(function (response) {
        loggedUser = response.data;
        return loggedUser;
    });

    var userSessionService = {};
    
    userSessionService.getLoggedUser = function () {
        return loggedUser;
    };

    userSessionService.getLoggedUserPromise = function () {
        return loggedUserPromise;
    };

    userSessionService.isLogged = function () {
        return angular.isObject(loggedUser);
    };

    userSessionService.isNotLogged = function () {
        return !userSessionService.isLogged();
    };

    userSessionService.login = function (user) {
        loggedUserPromise = $http.post('api/users', angular.toJson(user)).then(function (response) {
            loggedUser = response.data;
            return response.data;
        });
        return loggedUserPromise;
    };

    userSessionService.resetLoggedUser = function () {
        loggedUser = null;
    };

    userSessionService.logout = function () {
        return $http.get('api/users/logout').then(function () {
            userSessionService.resetLoggedUser();
            //this line reloads page and we are sure that there are not leftovers of logged user anywhere.
            $window.location = '/';
        });
    };

    userSessionService.getLoggedUserName = function () {
        if (loggedUser) {
            return loggedUser.login;
        } else {
            return '';
        }
    };

    userSessionService.saveTarget = function (targetState, targetParams) {
        target = {targetState: targetState, targetParams: targetParams};
    };

    userSessionService.loadTarget = function () {
        var result = target;
        target = null;
        return result;
    };

    userSessionService.updateLogin = function (login) {
        if (loggedUser) {
            loggedUser.login = login;
        } else {
            $log.warn('Trying to updated login but user is null');
        }
    };

    userSessionService.updateEmail = function (email) {
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