export default ngModule => {
  ngModule.controller('PasswordResetCtrl', ($scope, PasswordResetService, FlashService, $state, $stateParams, NotificationsService) => {

    $scope.login = '';
    $scope.password = '';
    $scope.repeatPassword = '';

    $scope.recoverPassword = () => {
      $scope.passwordResetRequestForm.login.$dirty = true;

      if ($scope.passwordResetRequestForm.$valid) {
        PasswordResetService.beginResetProcess($scope.login).then(beginResetProcessSuccess, NotificationsService.showError);
      }
    };

    function beginResetProcessSuccess() {
      FlashService.set('E-mail with link to reset your password has been sent. Please check your mailbox.');
      $state.go('home');
    }

    $scope.resetPassword = () => {
      $scope.changePasswordForm.password.$dirty = true;
      $scope.changePasswordForm.repeatPassword.$dirty = true;

      if ($scope.changePasswordForm.$valid && !$scope.changePasswordForm.repeatPassword.$error.repeat) {
        if ($stateParams.code) {
          PasswordResetService.changePassword($stateParams.code, $scope.password).then(onChangeSuccess, onChangeFailure);
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

  })
}
