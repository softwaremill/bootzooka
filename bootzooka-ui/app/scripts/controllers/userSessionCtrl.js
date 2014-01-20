"use strict";

angular.module('smlBootzooka.session').controller('UserSessionCtrl', function UserSessionCtrl($scope, $location, UserSessionService) {
    $scope.isLogged = function () {
        return UserSessionService.isLogged();
    };

    $scope.isNotLogged = function () {
        return UserSessionService.isNotLogged();
    };

    $scope.getLoggedUserName = function () {
        return UserSessionService.getLoggedUserName();
    };

    $scope.logout = function () {
        UserSessionService.logout(function () {
            $location.path("/");
        });
    };
});