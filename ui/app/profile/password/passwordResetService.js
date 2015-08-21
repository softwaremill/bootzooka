'use strict';

angular.module('smlBootzooka.profile').factory('PasswordResetService', function ($q, $http) {
    var passwordresetService = {};

    passwordresetService.beginResetProcess = function (login) {
        return $http.post('api/passwordreset', {login: login}).then(function (response) {
            if (response.data.value !== 'success') {
                return $q.reject(response.data.value);
            }
            return response;
        });
    };

    passwordresetService.changePassword = function (code, password) {
        return $http.post('api/passwordreset/' + code, {code: code, password: password});
    };

    return passwordresetService;
});