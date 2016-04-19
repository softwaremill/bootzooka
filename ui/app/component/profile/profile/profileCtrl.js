export default ngModule => {
  ngModule.controller('ProfileCtrl', function ($q, $scope, ProfileService, NotificationsService, user, UserSessionService) {
    $scope.user = {
      login: user.login,
      email: user.email
    };

    $scope.changeLogin = () => {
      if (this.shouldPerformLoginChange()) {
        ProfileService.changeLogin($scope.user.login).then(function () {
          UserSessionService.updateLogin($scope.user.login);
          NotificationsService.showSuccess('Login changed!');
          $scope.profileForm.login.$dirty = false;
          $scope.profileForm.login.$pristine = true;
        });
      }
    };

    this.shouldPerformLoginChange = () =>
    $scope.profileForm.login.$dirty && $scope.user.login !== user.login && $scope.profileForm.login.$valid;


    $scope.changeEmail = () => {
      if (this.shouldPerformEmailChange()) {
        ProfileService.changeEmail($scope.user.email).then(function () {
          UserSessionService.updateEmail($scope.user.email);
          NotificationsService.showSuccess('Email changed!');
          $scope.profileForm.email.$dirty = false;
          $scope.profileForm.email.$pristine = true;
        });
      }
    };

    this.shouldPerformEmailChange = () =>
    $scope.profileForm.email.$dirty && $scope.user.email !== user.email && $scope.profileForm.email.$valid;

    $scope.currentPassword = undefined;
    $scope.newPassword = undefined;
    $scope.newPasswordRepeated = undefined;

    $scope.changePassword = () => {
      $scope.passwordChangeForm.currentPassword.$dirty = true;
      $scope.passwordChangeForm.newPassword.$dirty = true;
      $scope.passwordChangeForm.newPasswordRepeated.$dirty = true;
      if ($scope.passwordChangeForm.$valid) {
        ProfileService.changePassword($scope.currentPassword, $scope.newPassword).then(() => {
          NotificationsService.showSuccess('Password changed!');
          $scope.passwordChangeForm.$setPristine();
          $scope.currentPassword = undefined;
          $scope.newPassword = undefined;
          $scope.newPasswordRepeated = undefined;
        }, NotificationsService.showError);
      }
    };
  })
}
