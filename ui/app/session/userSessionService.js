"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource, $http, $rootScope) {

    var loggedUser = null;
    var target = null;

    var loggedUserPromise = $http.get('rest/users').success(function (user) {
        loggedUser = user;
        return user;
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

    userSessionService.login = function (user, successFunction, errorFunction) {
        $http.post('rest/users', angular.toJson(user)).success(function (data) {
            loggedUser = data;
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        }, errorFunction);
    };

    userSessionService.resetLoggedUser = function () {
        loggedUser = null;
    };

    userSessionService.logout = function (successFunction) {
        $http.get('rest/users/logout').then(function (data) {
            userSessionService.resetLoggedUser();
            if (typeof successFunction === "function") {
                successFunction(data);
            }
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

    $rootScope.isLogged = function () {
        return userSessionService.isLogged();
    };

    $rootScope.isNotLogged = function () {
        return userSessionService.isNotLogged();
    };

    return userSessionService;
});