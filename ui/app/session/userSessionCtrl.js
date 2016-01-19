'use strict';

angular.module('smlBootzooka.session').controller('UserSessionCtrl', ($scope, UserSessionService) => {

  $scope.getLoggedUserName = () => UserSessionService.getLoggedUserName();

  $scope.logout = () => UserSessionService.logout();

});
