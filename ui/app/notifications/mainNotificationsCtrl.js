'use strict';

angular.module('smlBootzooka.notifications').controller('MainNotificationsCtrl', ($scope, NotificationsService) => {
  $scope.notificationsService = NotificationsService;
});
