export default ngModule => {
  ngModule.controller('LoginCtrl', ($scope, UserSessionService, $state, $stateParams, NotificationsService) => {

    $scope.user = {};
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.rememberMe = false;

    $scope.login = () => {
      // set dirty to show error messages on empty fields when submit is clicked
      $scope.loginForm.login.$dirty = true;
      $scope.loginForm.password.$dirty = true;

      if ($scope.loginForm.$invalid === false) {
        UserSessionService.login($scope.user).then(loginOk, loginFailed);
      }
    };

    function loginOk() {
      var optionalRedirect = UserSessionService.loadTarget();
      if (optionalRedirect) {
        $state.go(optionalRedirect.targetState, optionalRedirect.targetParams);
      } else {
        $state.go('main');
      }
    }

    function loginFailed() {
      NotificationsService.showError('Invalid login and/or password.');
    }
  })
}
