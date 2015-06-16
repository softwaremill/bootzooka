"use strict";

angular.module('smlBootzooka.session').controller('UserSessionCtrl', function UserSessionCtrl($rootScope, $window, $scope, $location, UserSessionService, FlashService) {

    $scope.getLoggedUserName = function () {
        return UserSessionService.getLoggedUserName();
    };

    $scope.logout = function () {
        UserSessionService.logout(function () {
            $window.location = '/'
        });
    };
});