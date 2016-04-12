export default ngModule => {
  ngModule.controller('RegisterCtrl', ($scope, RegisterService, $state) => {

    $scope.user = {};
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.email = '';
    $scope.user.repeatPassword = '';

    $scope.register = () => {
      $scope.registerForm.login.$dirty = true;
      $scope.registerForm.password.$dirty = true;
      $scope.registerForm.email.$dirty = true;
      $scope.registerForm.repeatPassword.$dirty = true;

      if ($scope.registerForm.$valid && !$scope.registerForm.repeatPassword.$error.repeat) {
        let jsonUser = {}; // create dedicated object to pass only specific fields
        jsonUser.login = $scope.user.login;
        jsonUser.email = $scope.user.email;
        jsonUser.password = $scope.user.password;
        RegisterService.register(jsonUser).then(registerOk);
      }
    };

    function registerOk() {
      $state.go('login');
    }
  })
}
