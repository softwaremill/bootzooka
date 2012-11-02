angular.module('userService', ['ngResource']).
    factory('UserService', function($resource) {
        var UserService = $resource('/users', { },
            { }
        );

        return UserService;
    });