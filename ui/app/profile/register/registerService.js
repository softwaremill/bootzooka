"use strict";

angular.module("smlBootzooka.profile").factory('RegisterService', function ($http, FlashService) {

    var registerService = {};

    registerService.register = function (user, successFunction, errorFunction) {
        $http.post('rest/users/register',angular.toJson(user)).success(function (data) {
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
