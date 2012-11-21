angular.module('userSessionService', ['ngResource']).
    factory('UserSessionService', function($resource, $rootScope) {

        var userSessionService = new Object();

        userSessionService.userService = $resource('rest/users/', { },
            {
                login: {method: 'POST'},
                valid: {method: 'GET'}
            }
        );

        userSessionService.logoutService = $resource('rest/users/logout', { }, { } );

        $rootScope.loggedUser = null;

        userSessionService.isLogged = function() {
            return $rootScope.loggedUser != null;
        };

        userSessionService.isNotLogged = function() {
            return $rootScope.loggedUser == null;
        };

        userSessionService.login = function(user, successFunction, errorFunction) {
            userSessionService.userService.login(angular.toJson(user), function(data) {

                    $rootScope.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                },
                errorFunction);
        };

        userSessionService.logout = function(user, successFunction) {
            userSessionService.logoutService.query(null, function(data) {
                $rootScope.loggedUser = null;
                if(successFunction != null) {
                    successFunction(data);
                }
            });
        };

        userSessionService.validate = function(successFunction) {
            userSessionService.userService.valid(
                function(data) {
                    $rootScope.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                }
            );
        };

        return userSessionService;
    });