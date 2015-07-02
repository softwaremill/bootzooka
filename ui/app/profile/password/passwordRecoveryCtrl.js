'use strict';

angular.module('smlBootzooka.profile').controller('PasswordRecoveryCtrl', function PasswordRecoveryCtrl($scope, PasswordRecoveryService, FlashService, $state, $stateParams, NotificationsService) {

    $scope.login = '';
    $scope.password = '';
    $scope.repeatPassword = '';

    $scope.recoverPassword = function () {
        $scope.passwordResetRequestForm.login.$dirty = true;

        if ($scope.passwordResetRequestForm.$valid) {
            PasswordRecoveryService.beginResetProcess($scope.login).then(beginResetProcessSuccess, NotificationsService.showError);
        }
    };

    function beginResetProcessSuccess() {
        FlashService.set('E-mail with link to reset your password has been sent. Please check your mailbox.');
        $state.go('home');
    }

    $scope.resetPassword = function () {
        $scope.changePasswordForm.password.$dirty = true;
        $scope.changePasswordForm.repeatPassword.$dirty = true;

        if ($scope.changePasswordForm.$valid && !$scope.changePasswordForm.repeatPassword.$error.repeat) {
            if ($stateParams.code) {
                PasswordRecoveryService.changePassword($stateParams.code, $scope.password).then(onChangeSuccess, onChangeFailure);
            } else {
                onChangeFailure('Wrong or malformed password recovery code.');
            }
        }
    };

    function onChangeSuccess() {
        FlashService.set('Your password has been changed');
        $state.go('home');
    }

    function onChangeFailure(error) {
        NotificationsService.showError(error);
        $state.go('recover-lost-password');
    }

});