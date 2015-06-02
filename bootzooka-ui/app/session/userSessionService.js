"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource, $cookies) {

    var self = this;

    self.userResource = $resource('rest/users/', {}, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    }, {});

    self.logoutResource = $resource('rest/users/logout', {}, {}, {});

    var userSessionService = {
        loggedUser: null
    };

    userSessionService.isLogged = function () {
        if (userSessionService.loggedUser !== null) {
            return true;

        }
        if (!angular.isUndefined($cookies["scentry.auth.default.user"])) {
            userSessionService.validate()
            return true;
        }
        return false;
    };

    userSessionService.isNotLogged = function () {
        return !userSessionService.isLogged();
    };

    userSessionService.login = function (user, successFunction, errorFunction) {
        self.userResource.login(angular.toJson(user), function (data) {
            userSessionService.loggedUser = data;
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        }, errorFunction);
    };

    userSessionService.logout = function (successFunction) {
        self.logoutResource.query(null, function (data) {
            userSessionService.loggedUser = null;
            delete $cookies["scentry.auth.default.user"];
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        });
    };

    userSessionService.validate = function (successFunction) {
        self.userResource.valid(function (data) {
            userSessionService.loggedUser = data;
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        });
    };

    userSessionService.getLoggedUserName = function () {
        if (userSessionService.loggedUser) {
            return userSessionService.loggedUser.login;
        } else {
            return "";
        }
    };

    return userSessionService;
});