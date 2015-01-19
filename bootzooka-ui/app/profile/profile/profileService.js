angular.module("smlBootzooka.profile").factory("ProfileService", function ($resource) {
    var profileService = {};

    this.profileResource = $resource("rest/users", {}, {
        'changeLogin': {method: 'PATCH'},
        'changeEmail': {method: 'PATCH'},
        'changePassword': {method: 'PATCH'}
    }, {});

    this.changePasswordResource = $resource("rest/users/changepassword", {}, {
        'changePassword': {method: 'POST'}
    }, {});

    var self = this;

    profileService.changeLogin = function (newLogin, onSuccess, onError) {
        self.profileResource.changeLogin({login: newLogin}, function () {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    profileService.changeEmail = function (newEmail, onSuccess, onError) {
        self.profileResource.changeEmail({email: newEmail}, function () {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    profileService.changePassword = function (currentPassword, newPassword, onSuccess, onError) {
        self.changePasswordResource.changePassword({currentPassword: currentPassword, newPassword: newPassword}, function () {
            onSuccess();
        }, function (error) {
            onError(error.data);
        });
    };

    return profileService;
});