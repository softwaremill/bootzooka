'use strict';

angular.module('smlBootzooka.profile').factory('RegisterService', function ($http, $q, FlashService) {

    var registerService = {};

    registerService.register = function (user) {
        return $http.post('api/users/register', angular.toJson(user)).then(function (response) {
            if (response.data.value === 'success') {
                FlashService.set('User registered successfully! Please check your e-mail for confirmation.');
                return response.data;
            } else {
                return $q.reject(response);
            }
        });
    };

    return registerService;
});
