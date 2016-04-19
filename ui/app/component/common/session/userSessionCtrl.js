export default ngModule => {
  ngModule.controller('UserSessionCtrl', ($scope, UserSessionService) => {

    $scope.isNotLogged = () => UserSessionService.isNotLogged();
    $scope.isLogged = () => UserSessionService.isLogged();
    $scope.getLoggedUserName = () => UserSessionService.getLoggedUserName();
    $scope.logout = () => UserSessionService.logout();
  });
};
