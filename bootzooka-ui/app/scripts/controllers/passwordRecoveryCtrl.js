"use strict";

angular.module('smlBootzooka.profile').controller('PasswordRecoveryCtrl', function PasswordRecoveryCtrl($scope, PasswordRecoveryService, FlashService, $location, $routeParams) {
    var self = this;

    $scope.login = '';
    $scope.password = '';
    $scope.repeatPassword = '';

    $scope.recoverPassword = function () {
        $scope.passwordResetRequestForm.login.$dirty = true;

        if ($scope.passwordResetRequestForm.$valid) {
            PasswordRecoveryService.beginResetProcess($scope.login, self.success, self.failure);
        }
    };

    this.success = function () {
        FlashService.set("E-mail with link to reset your password has been sent. Please check your mailbox.");
        $location.path("");
    };

    this.failure = function (message) {
        showErrorMessage(message);
    };

    $scope.resetPassword = function () {
        $scope.changePasswordForm.password.$dirty = true;
        $scope.changePasswordForm.repeatPassword.$dirty = true;

        if ($scope.changePasswordForm.$valid && !$scope.changePasswordForm.repeatPassword.$error.repeat) {
            PasswordRecoveryService.changePassword($routeParams.code, $scope.password, self.onChangeSuccess, self.onChangeFailure);
        }
    };

    this.onChangeSuccess = function () {
        FlashService.set("Your password has been changed");
        $location.search("code", null);
        $location.path("");
    };

    this.onChangeFailure = function (error) {
        showErrorMessage(error.data.value);
        $location.search("code", null);
        $location.path("recover-lost-password");
    };
});