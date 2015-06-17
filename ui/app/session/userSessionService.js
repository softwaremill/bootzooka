"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource, $http, $rootScope) {

    var loggedUser = null;
    var target = null;

    var loggedUserPromise = $http.get('rest/users').then(function (response) {
        loggedUser = response.data;
        return loggedUser;
    });

    var userSessionService = {
        loggedUser: function () {
            return loggedUser;
        },
        loggedUserPromise: function () {
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
        return $http.post('rest/users', angular.toJson(user)).then(function (response) {
            loggedUser = response.data;
            return response.data;
        });
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

    $rootScope.isLogged = userSessionService.isLogged;
    $rootScope.isNotLogged = userSessionService.isNotLogged;
    
    return userSessionService;
})
;