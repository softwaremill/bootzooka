"use strict";

angular.module('smlBootzooka.session').controller('UserSessionCtrl', function UserSessionCtrl($rootScope, $window, $scope, $location, UserSessionService, FlashService) {
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
            $window.location = '/'
        });
    };

    $rootScope.$on('401', function () {
        if (UserSessionService.isLogged()) {
            UserSessionService.logout();
            FlashService.set('Your session timed out. Please login again.');
        }
    });
});