"use strict";

angular.module("smlBootzooka.profile").factory("PasswordRecoveryService", function ($q, $http) {
    var passwordRecoveryService = {};

    passwordRecoveryService.beginResetProcess = function (login) {
        return $http.post("rest/passwordrecovery", {login: login}).then(function (response) {
            if (response.data.value !== 'success') {
                return $q.reject(response.data.value);
            }
            return response;
        });
    };

    passwordRecoveryService.changePassword = function (code, password) {
        return $http.post("rest/passwordrecovery/" + code, {code: code, password: password});
    };

    return passwordRecoveryService;
});