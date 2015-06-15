"use strict";

angular.module("smlBootzooka.session").factory('UserSessionService', function ($resource) {

    var self = this;

    self.userResource = $resource('rest/users/', {}, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    }, {});

    self.logoutResource = $resource('rest/users/logout', {}, {}, {});

    var initPromise = function () {
        self.userResource.valid().$promise.then(function (data) {
            userSessionService.loggedUser.init(data);
        });
    };

    var userSessionService = {
        loggedUser: {
            login: "",
            email: "",
            isLogged: false,
            init: function (user) {
                this.email = user.email;
                this.login = user.login;
                this.isLogged = true;
            },
            reset: function () {
                this.email = "";
                this.login = "";
                this.isLogged = false;
            }
        },
        loggedUserPromise: initPromise()
    };

    userSessionService.isLogged = function () {
        return userSessionService.loggedUser.isLogged;
    };

    userSessionService.isNotLogged = function () {
        return !userSessionService.isLogged();
    };

    userSessionService.login = function (user, successFunction, errorFunction) {
        self.userResource.login(angular.toJson(user), function (data) {
            userSessionService.loggedUser.init(data);
            if (typeof successFunction === "function") {
                successFunction(data);
            }
        }, errorFunction);
    };

    userSessionService.logout = function (successFunction) {
        self.logoutResource.query(null, function (data) {
            userSessionService.loggedUser.reset();
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
})
;