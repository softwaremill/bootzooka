var services = angular.module('smlBootstrap.services', ['ngResource']);

services.factory('EntriesService', function ($resource) {

    var entriesService = {};

    entriesService.crudService = $resource('rest/entries/:id', { }, {
        insert: { method: "PUT"}
    });

    entriesService.counter = $resource("/rest/entries/count");


    entriesService.loadAll = function (successFunction) {
        entriesService.crudService.query(null, successFunction);
    };

    entriesService.addNew = function (entryText, successFunction) {
        var json = {};
        json.text = entryText;
        entriesService.crudService.insert(angular.toJson(json), successFunction);
    };

    entriesService.load = function (logObjectId, successFunction) {
        entriesService.crudService.get({id: logObjectId}, successFunction);
    };

    entriesService.update = function (logObject) {
        var json = {};
        json.text = logObject.text;
        json.id = logObject.id;
        entriesService.crudService.save(angular.toJson(json));
    };

    entriesService.deleteEntry = function (logObjectId, successFunction) {
        entriesService.crudService.remove({id: logObjectId}, successFunction);
    };

    entriesService.count = function (successFunction) {
        entriesService.counter.get(successFunction);
    };

    return entriesService;
});


services.factory('UserSessionService', function ($resource) {

    var userSessionService = {
        loggedUser: null
    };

    userSessionService.userService = $resource('rest/users/', { }, {
        login: {method: 'POST'},
        valid: {method: 'GET'}
    });

    userSessionService.logoutService = $resource('rest/users/logout', { }, { });

    userSessionService.isLogged = function () {
        return userSessionService.loggedUser != null;
    };

    userSessionService.isNotLogged = function () {
        return userSessionService.loggedUser == null;
    };

    userSessionService.login = function (user, successFunction, errorFunction) {
        userSessionService.userService.login(angular.toJson(user), function (data) {

                    userSessionService.loggedUser = data;
                    if (successFunction != null) {
                        successFunction(data);
                    }
                },
                errorFunction)
    };

    userSessionService.logout = function (user, successFunction) {
        userSessionService.logoutService.query(null, function (data) {
            userSessionService.loggedUser = null;
            if (successFunction != null) {
                successFunction(data);
            }
        });
    };

    userSessionService.validate = function (successFunction) {
        userSessionService.userService.valid(
                function (data) {
                    userSessionService.loggedUser = data;
                    if (successFunction != null) {
                        successFunction(data);
                    }
                }
        );
    };

    userSessionService.getLoggedUserName = function () {
        if (userSessionService.loggedUser) {
            return userSessionService.loggedUser.login;
        }
        else {
            return "";
        }
    };

    return userSessionService;
});


services.factory('UtilService', function ($resource) {

    var utilService = $resource('/rest/uptime');

    utilService.loadUptime = function (successFunction) {
        return utilService.get(successFunction);
    };

    return utilService;
});

services.factory('RegisterService', function ($resource, FlashService) {

    var registerService = {};

    registerService.backend = $resource('rest/users/register', { }, {
        insert: {method: 'PUT'}
    });

    registerService.register = function (user, successFunction, errorFunction) {
        registerService.backend.insert(angular.toJson(user), function (data) {
                    if (angular.equals(data.value, 'success')) {
                        FlashService.set("User registered successfully!");
                        successFunction();
                    } else {
                        errorFunction(data.value)
                    }
                }
        )
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
