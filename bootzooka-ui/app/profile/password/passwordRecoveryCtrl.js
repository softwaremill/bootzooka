"use strict";

angular.module('smlBootzooka.profile').controller('PasswordRecoveryCtrl', function PasswordRecoveryCtrl($scope, PasswordRecoveryService, FlashService, $state, $stateParams, NotificationsService) {
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
        $state.go('home');
    };

    this.failure = function (message) {
        NotificationsService.showError(message);
    };

    $scope.resetPassword = function () {
        $scope.changePasswordForm.password.$dirty = true;
        $scope.changePasswordForm.repeatPassword.$dirty = true;

        if ($scope.changePasswordForm.$valid && !$scope.changePasswordForm.repeatPassword.$error.repeat) {
            PasswordRecoveryService.changePassword($stateParams.code, $scope.password, self.onChangeSuccess, self.onChangeFailure);
        }
    };

    this.onChangeSuccess = function () {
        FlashService.set("Your password has been changed");
        $state.go('home');
    };

    this.onChangeFailure = function (error) {
        NotificationsService.showError(error.data.value);
        $state.go('recover-lost-password');
    };
});