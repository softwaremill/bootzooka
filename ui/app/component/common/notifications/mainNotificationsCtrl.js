export default ngModule => {
  ngModule.controller('MainNotificationsCtrl', ($scope, NotificationsService) => {
    $scope.notificationsService = NotificationsService;
  });
};
