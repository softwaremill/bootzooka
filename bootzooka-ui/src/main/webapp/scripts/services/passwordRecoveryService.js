"use strict";

angular.module("smlBootstrap.profile").factory("PasswordRecoveryService", function ($resource) {
    var passwordRecoveryService = {};

    this.recoveryResource = $resource("rest/passwordrecovery", {}, {
        'resetPassword': {method: "POST"}
    });

    this.changeResource = $resource("rest/passwordrecovery/:code", {code: "@code"}, {
        'changePassword': {method: "POST"}
    });

    var self = this;

    passwordRecoveryService.beginResetProcess = function (login, onComplete) {
        self.recoveryResource.resetPassword({login: login}, function () {
            onComplete();
        });
    };

    passwordRecoveryService.changePassword = function (code, password, onComplete, onError) {
        self.changeResource.changePassword({code: code, password: password}, function (data) {
            onComplete();
        }, function (error) {
            onError(error);
        })
    };

    return passwordRecoveryService;
});