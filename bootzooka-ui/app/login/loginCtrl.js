"use strict";

angular.module('smlBootzooka.profile').controller('LoginCtrl', function LoginCtrl($scope, UserSessionService, $location, $stateParams) {

    var self = this;

    $scope.user = {};
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.rememberme = false;

    $scope.login = function () {
        // set dirty to show error messages on empty fields when submit is clicked
        $scope.loginForm.login.$dirty = true;
        $scope.loginForm.password.$dirty = true;

      if ($scope.loginForm.$invalid === false) {
            UserSessionService.login($scope.user, self.loginOk, self.loginFailed);
        }
    };


    this.loginOk = function () {
        var optionalRedirect = $stateParams.page;
        if (typeof optionalRedirect !== "undefined") {
            $location.search("page", null);
            $location.path(optionalRedirect);
        } else {
            $location.path("/main");
        }
    };

    this.loginFailed = function () {
        bootzooka.utils.showErrorMessage("Invalid login and/or password.");
    };
});