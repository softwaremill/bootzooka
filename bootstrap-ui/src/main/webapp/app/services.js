"use strict";

var services = angular.module('smlBootstrap.services', ['ngResource', 'ngCookies']);

var dontBlockOnAjaxHeader = { "dontBlockPageOnAjax": "true" };
var nonArrayGetWithoutBlockOnAjax = { method: "GET", isArray: false, headers: dontBlockOnAjaxHeader };


services.factory('EntriesService', function ($resource) {

    var self = this;

    self.entriesCrudResource = $resource('rest/entries/:id', { }, {
        insert: { method: "PUT"},
        query: nonArrayGetWithoutBlockOnAjax
    });

    self.counterResource = $resource("/rest/entries/count", { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    self.newEntriesCounterResource = $resource("/rest/entries/count-newer/:time", { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    var entriesService = {};

    entriesService.loadAll = function (successFunction) {
        self.entriesCrudResource.query(null, successFunction);
    };

    entriesService.addNew = function (entryText, successFunction) {
        var json = {};
        json.text = entryText;
        self.entriesCrudResource.save(angular.toJson(json), successFunction);
    };

    entriesService.load = function (logObjectId, successFunction) {
        self.entriesCrudResource.get({id: logObjectId}, successFunction);
    };

    entriesService.update = function (logObject) {
        var json = {};
        json.text = logObject.text;
        json.id = logObject.id;
        self.entriesCrudResource.insert(angular.toJson(json));
    };

    entriesService.deleteEntry = function (logObjectId, successFunction) {
        self.entriesCrudResource.remove({id: logObjectId}, successFunction);
    };

    entriesService.count = function (successFunction) {
        self.counterResource.get(successFunction);
    };

    entriesService.countNewEntries = function (timestamp, successFunction) {
        self.newEntriesCounterResource.get({time: timestamp}, successFunction);
    };

    return entriesService;
});


services.factory('UserSessionService', function ($resource, $cookies) {

    var self = this;

    self.userResource = $resource('rest/users/', { }, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    });

    self.logoutResource = $resource('rest/users/logout', { }, { });


    var userSessionService = {
        loggedUser: null
    };

    userSessionService.isLogged = function () {
        return $cookies["scentry.auth.default.user"] !== undefined;
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


services.factory('UtilService', function ($resource) {

    var self = this;

    self.utilResource = $resource('/rest/uptime', { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    var utilService = {};

    utilService.loadUptime = function (successFunction) {
        return self.utilResource.get(successFunction);
    };

    return utilService;
});

services.factory('RegisterService', function ($resource, FlashService) {

    var self = this;
    self.registerResource = $resource('rest/users/register');

    var registerService = {};

    registerService.register = function (user, successFunction, errorFunction) {
        self.registerResource.save(angular.toJson(user), function (data) {
            if (angular.equals(data.value, 'success')) {
                FlashService.set("User registered successfully! Please check your e-mail for confirmation.");
                successFunction();
            } else {
                errorFunction(data.value);
            }
        });
    };

    return registerService;
});

services.factory("FlashService", function () {

    var queue = [];

    return {
        set: function (message) {
            queue.push(message);
        },
        get: function () {
            return queue.shift();
        }
    };
});


services.factory("PasswordRecoveryService", function ($resource) {
    var passwordRecoveryService = {};

    this.recoveryResource = $resource("rest/passwordrecovery", {}, {
        'resetPassword': {method: "POST"}
    });

    this.changeResource = $resource("rest/passwordrecovery/:code", {code: "@code"}, {
        'changePassword': {method: "POST"}
    });

    var self = this;

    passwordRecoveryService.beginResetProcess = function (login, onComplete) {
        self.recoveryResource.resetPassword({login: login}, function () {
            onComplete();
        });
    };

    passwordRecoveryService.changePassword = function (code, password, onComplete, onError) {
        self.changeResource.changePassword({code: code, password: password}, function (data) {
            onComplete();
        }, function (error) {
            onError(error);
        })
    };

    return passwordRecoveryService;
});

services.factory("ProfileService", function ($resource) {
    var profileService = {};

    this.profileResource = $resource("rest/users", {}, {
        'changeLogin': {method: 'PATCH'},
        'changeEmail': {method: 'PATCH'},
        'changePassword': {method: 'PATCH'}
    });

    this.changePasswordResource = $resource("rest/users/changepassword", {}, {
        'changePassword': {method: 'POST'}
    })

    var self = this;

    profileService.changeLogin = function (newLogin, onSuccess, onError) {
        self.profileResource.changeLogin({login: newLogin}, function (result) {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    profileService.changeEmail = function (newEmail, onSuccess, onError) {
        self.profileResource.changeEmail({email: newEmail}, function (result) {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    profileService.changePassword = function (currentPassword, newPassword, onSuccess, onError) {
        self.changePasswordResource.changePassword({currentPassword: currentPassword, newPassword: newPassword}, function (result) {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    return profileService;
});