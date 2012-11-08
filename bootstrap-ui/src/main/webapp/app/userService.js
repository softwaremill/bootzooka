angular.module('userService', ['ngResource']).
    factory('UserService', function($resource) {
        var UserService = $resource('rest/users/', { },
            {
                login: {method: 'POST'},
                valid: {method: 'GET'}
            }
        );

        UserService.loginUser = function(user, successFunction, errorFunction) {
            UserService.login(angular.toJson(user), successFunction, errorFunction);
        };

        UserService.validate = function(successFunction, errorFunction) {
            UserService.valid(successFunction, errorFunction);
        };

        return UserService;
    });


angular.module('logoutService', ['ngResource']).
    factory('LogoutService', function($resource) {
        var LogoutService = $resource('rest/users/logout', { }, { } );

        LogoutService.logout = function(successFunction) {
            LogoutService.query(null, successFunction);
        };

        return LogoutService;
    });



