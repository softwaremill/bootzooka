"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource) {

    var self = this;

    self.userResource = $resource('rest/users/', {}, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    }, {});

    self.logoutResource = $resource('rest/users/logout', {}, {}, {});

    var loggedUser = null;
    var initPromise = function () {
        return self.userResource.valid().$promise.then(function (user) {
            loggedUser = user;
            return user;
        });
    };

    var userSessionService = {
        loggedUser: function () {
            return loggedUser;
        },
        loggedUserPromise: initPromise()
    };

    userSessionService.isLogged = function () {
        return angular.isObject(loggedUser);
    };

    userSessionService.isNotLogged = function () {
        return !userSessionService.isLogged();
    };

    userSessionService.login = function (user, successFunction, errorFunction) {
        self.userResource.login(angular.toJson(user), function (data) {
            loggedUser = user;
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        }, errorFunction);
    };

    userSessionService.logout = function (successFunction) {
        self.logoutResource.query(null, function (data) {
            loggedUser = null;
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

    return userSessionService;
})
;