angular.module('smlBootzooka.profile').controller("ProfileCtrl", function ProfileCtrl($scope, UserSessionService, ProfileService) {
    $scope.login = UserSessionService.loggedUser.login.concat();
    $scope.email = UserSessionService.loggedUser.email.concat();

    var self = this;

    $scope.changeLogin = function () {
        if (self.shouldPerformLoginChange()) {
            ProfileService.changeLogin($scope.login, function () {
                UserSessionService.loggedUser.login = $scope.login.concat();
                showInfoMessage("Login changed!");
                $scope.profileForm.login.$dirty = false;
                $scope.profileForm.login.$pristine = true;
            }, function (error) {
                showErrorMessage(error.value);
            });
        }
    };

    this.shouldPerformLoginChange = function () {
        return $scope.profileForm.login.$dirty && $scope.login != UserSessionService.loggedUser.login && $scope.profileForm.login.$valid;
    };

    $scope.changeEmail = function () {
        if (self.shouldPerformEmailChange()) {
            ProfileService.changeEmail($scope.email, function () {
                UserSessionService.loggedUser.email = $scope.email;
                showInfoMessage("Email changed!");
                $scope.profileForm.email.$dirty = false;
                $scope.profileForm.email.$pristine = true;
            }, function (error) {
                showErrorMessage(error.value);
            });
        }
    };

    this.shouldPerformEmailChange = function () {
        return $scope.profileForm.email.$dirty && $scope.email != UserSessionService.loggedUser.email && $scope.profileForm.email.$valid;
    };

    $scope.currentPassword = undefined;
    $scope.newPassword = undefined;
    $scope.newPasswordRepeated = undefined;

    $scope.changePassword = function () {
        $scope.passwordChangeForm.currentPassword.$dirty = true;
        $scope.passwordChangeForm.newPassword.$dirty = true;
        $scope.passwordChangeForm.newPasswordRepeated.$dirty = true;
        if ($scope.passwordChangeForm.$valid) {
            ProfileService.changePassword($scope.currentPassword, $scope.newPassword, function () {
                showInfoMessage("Password changed!");
                $scope.passwordChangeForm.$setPristine();
                $scope.currentPassword = undefined;
                $scope.newPassword = undefined;
                $scope.newPasswordRepeated = undefined;
            }, function (error) {
                showErrorMessage(error.value);
            });
        }
    };
});