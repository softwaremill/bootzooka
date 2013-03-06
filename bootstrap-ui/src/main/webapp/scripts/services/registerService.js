"use strict";

angular.module("smlBootstrap.profile").factory('RegisterService', function ($resource, FlashService) {

    var self = this;
    self.registerResource = $resource('rest/users/register');

    var registerService = {};

    registerService.register = function (user, successFunction, errorFunction) {
        self.registerResource.save(angular.toJson(user), function (data) {
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
