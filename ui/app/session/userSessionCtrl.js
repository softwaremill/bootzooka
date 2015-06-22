'use strict';

angular.module('smlBootzooka.session').controller('UserSessionCtrl', function UserSessionCtrl($scope, UserSessionService) {

    $scope.getLoggedUserName = function () {
        return UserSessionService.getLoggedUserName();
    };

    $scope.logout = function () {
        UserSessionService.logout();
    };
});