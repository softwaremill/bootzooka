angular.module('entriesService', ['ngResource']).
    factory('EntriesService', function($resource) {

        var entriesService = new Object();

        entriesService.crudService = $resource('rest/entries/:id', { }, {
            insert: { method: "PUT"} }
        );

        entriesService.counter = $resource("/rest/entries/count");


        entriesService.loadAll = function(successFunction) {
            entriesService.crudService.query(null, successFunction);
        };

        entriesService.addNew = function (entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            entriesService.crudService.insert(angular.toJson(json), successFunction);
        };

        entriesService.load = function(logObjectId, successFunction) {
            entriesService.crudService.get({id: logObjectId}, successFunction);
        };

        entriesService.update =  function(logObject) {
            var json = new Object();
            json.text = logObject.text;
            json.id = logObject.id;
            entriesService.crudService.save(angular.toJson(json));
        };

        entriesService.deleteEntry = function(logObjectId, successFunction) {
            entriesService.crudService.remove({id: logObjectId}, successFunction);
        };

        entriesService.count = function(successFunction) {
            entriesService.counter.get(successFunction);
        };

        return entriesService;
    });


angular.module('userSessionService', ['ngResource']).
    factory('UserSessionService', function($resource, $rootScope) {

        var userSessionService = {
            loggedUser: null
        }

        userSessionService.userService = $resource('rest/users/', { },
            {
                login: {method: 'POST'},
                valid: {method: 'GET'}
            }
        );

        userSessionService.logoutService = $resource('rest/users/logout', { }, { } );

        userSessionService.isLogged = function() {
            return userSessionService.loggedUser != null;
        };

        userSessionService.isNotLogged = function() {
            return userSessionService.loggedUser == null;
        };

        userSessionService.login = function(user, successFunction, errorFunction) {
            userSessionService.userService.login(angular.toJson(user), function(data) {

                    userSessionService.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                },
                errorFunction);
        };

        userSessionService.logout = function(user, successFunction) {
            userSessionService.logoutService.query(null, function(data) {
                userSessionService.loggedUser = null;
                if(successFunction != null) {
                    successFunction(data);
                }
            });
        };

        userSessionService.validate = function(successFunction) {
            userSessionService.userService.valid(
                function(data) {
                    userSessionService.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                }
            );
        };

        userSessionService.getLoggedUserName = function() {
            if(userSessionService.loggedUser) {
                return userSessionService.loggedUser.login;
            }
            else {
                return "";
            }
        }

        return userSessionService;
    });


angular.module('utilService', ['ngResource']).
    factory('UtilService', function($resource) {
        var utilService = $resource('/rest/uptime');

        utilService.loadUptime = function(successFunction) {
            return utilService.get(successFunction);
        };

        return utilService;
    });
