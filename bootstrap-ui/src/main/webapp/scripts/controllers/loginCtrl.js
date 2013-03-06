angular.module('smlBootstrap.profile').controller('LoginCtrl', function LoginCtrl($scope, UserSessionService, $location, $routeParams) {

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
        var optionalRedir = $routeParams.page;
        if (typeof optionalRedir !== "undefined") {
            $location.search("page", null);
            $location.path(optionalRedir);
        } else {
            $location.path("");
        }
    };

    this.loginFailed = function () {
        showErrorMessage("Invalid login and/or password.");
    };
});