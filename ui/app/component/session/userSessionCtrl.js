export default ngModule => {
  ngModule.controller('UserSessionCtrl', ($scope, UserSessionService) => {

    $scope.getLoggedUserName = () => UserSessionService.getLoggedUserName();

    $scope.logout = () => UserSessionService.logout();

  });
};
