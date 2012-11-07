angular.module('userService', ['ngResource']).
    factory('UserService', function($resource) {
        var UserService = $resource('rest/users/', { },
            {
                login: {method: 'POST'}
            }
        );

        UserService.loginUser = function(user, successFunction, errorFunction) {
            UserService.login(angular.toJson(user), successFunction, errorFunction);
        };

        return UserService;
    });



