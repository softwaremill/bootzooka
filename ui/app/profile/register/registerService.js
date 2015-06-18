"use strict";

angular.module("smlBootzooka.profile").factory('RegisterService', function ($http, $q, FlashService) {

    var registerService = {};

    registerService.register = function (user) {
        return $http.post('rest/users/register', angular.toJson(user)).then(function (response) {
            if (angular.equals(response.data.value, 'success')) {
                FlashService.set("User registered successfully! Please check your e-mail for confirmation.");
                return $q.when(response.data);
            } else {
                return $q.reject(response);
            }
        });
    };

    return registerService;
});
