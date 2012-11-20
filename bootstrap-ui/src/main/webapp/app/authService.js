angular.module('authService', ['ngResource']).
    factory('AuthService', function($resource, $rootScope) {

        var AuthService = new Object();

        AuthService.userService = $resource('rest/users/', { },
            {
                login: {method: 'POST'},
                valid: {method: 'GET'}
            }
        );

        AuthService.logoutService = $resource('rest/users/logout', { }, { } );

        $rootScope.loggedUser = null;

        AuthService.isLogged = function() {
            return $rootScope.loggedUser != null;
        };

        AuthService.isNotLogged = function() {
            return $rootScope.loggedUser == null;
        };

        AuthService.login = function(user, successFunction, errorFunction) {
            AuthService.userService.login(angular.toJson(user), function(data) {

                    $rootScope.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                },
                errorFunction);
        };

        AuthService.logout = function(user, successFunction) {
            AuthService.logoutService.query(null, function(data) {
                $rootScope.loggedUser = null;
                if(successFunction != null) {
                    successFunction(data);
                }
            });
        };

        AuthService.validate = function(successFunction) {
            AuthService.userService.valid(
                function(data) {
                    $rootScope.loggedUser = data;
                    if(successFunction != null) {
                        successFunction(data);
                    }
                }
            );
        };

        return AuthService;
    });