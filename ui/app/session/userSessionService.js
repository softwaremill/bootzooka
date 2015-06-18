"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource, $http, $rootScope, $log) {

    var loggedUser = null;
    var target = null;

    var loggedUserPromise = $http.get('rest/users').then(function (response) {
        loggedUser = response.data;
        return loggedUser;
    });

    var userSessionService = {
        getLoggedUser: function () {
            return loggedUser;
        },
        getLoggedUserPromise: function () {
            return loggedUserPromise;
        }
    };

    userSessionService.isLogged = function () {
        return angular.isObject(loggedUser);
    };

    userSessionService.isNotLogged = function () {
        return !userSessionService.isLogged();
    };

    userSessionService.login = function (user) {
        loggedUserPromise = $http.post('rest/users', angular.toJson(user)).then(function (response) {
            loggedUser = response.data;
            return response.data;
        });
        return loggedUserPromise;
    };

    userSessionService.resetLoggedUser = function () {
        loggedUser = null;
    };

    userSessionService.logout = function () {
        return $http.get('rest/users/logout').then(function () {
            userSessionService.resetLoggedUser();
        });
    };

    userSessionService.getLoggedUserName = function () {
        if (loggedUser) {
            return loggedUser.login;
        } else {
            return "";
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
            $log.warn("Trying to updated login but user is null");
        }
    };

    userSessionService.updateEmail = function (email) {
        if (loggedUser) {
            loggedUser.email = email;
        } else {
            $log.warn("Trying to updated email but user is null");
        }
    };

    $rootScope.isLogged = userSessionService.isLogged;
    $rootScope.isNotLogged = userSessionService.isNotLogged;

    return userSessionService;
});