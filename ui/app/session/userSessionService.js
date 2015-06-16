"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource) {

    var self = this;

    self.userResource = $resource('rest/users/', {}, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    }, {});

    self.logoutResource = $resource('rest/users/logout', {}, {}, {});

    var loggedUser = null;
    var target = null;

    var loggedUserPromise = self.userResource.valid().$promise.then(function (user) {
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
        self.userResource.login(angular.toJson(user), function (data) {
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
        self.logoutResource.query(null, function (data) {
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

    return userSessionService;
});