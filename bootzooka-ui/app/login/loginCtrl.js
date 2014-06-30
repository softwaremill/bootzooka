"use strict";

angular.module('smlBootzooka.profile').controller('LoginCtrl', function LoginCtrl($scope, UserSessionService, $location, $routeParams) {

    var self = this;

    $scope.user = {};
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.rememberme = false;

    $scope.login = function () {
        // set dirty to show error messages on empty fields when submit is clicked
        $scope.loginForm.login.$dirty = true;
        $scope.loginForm.password.$dirty = true;
      console.log($scope.user);
      if ($scope.loginForm.$invalid === false) {
            UserSessionService.login($scope.user, self.loginOk, self.loginFailed);
        }
    };


    this.loginOk = function () {
        var optionalRedir = $routeParams.page;
        if (typeof optionalRedir !== "undefined") {
            $location.search("page", null);
            $location.path(optionalRedir);
        } else {
            $location.path("/main");
        }
    };

    this.loginFailed = function () {
        bootzooka.utils.showErrorMessage("Invalid login and/or password.");
    };
});