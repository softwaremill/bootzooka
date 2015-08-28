'use strict';

angular.module('smlBootzooka.profile').factory('PasswordResetService', function ($q, $http) {
    var passwordresetService = {};

    passwordresetService.beginResetProcess = function (login) {
        return $http.post('api/passwordreset', {login: login}).then(function (response) {
            if (response.data !== 'success') {
                return $q.reject(response.data);
            }
            return response;
        });
    };

    passwordresetService.changePassword = function (code, password) {
        return $http.post('api/passwordreset/' + code, {code: code, password: password});
    };

    return passwordresetService;
});