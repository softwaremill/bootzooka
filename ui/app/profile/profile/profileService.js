angular.module('smlBootzooka.profile').factory('ProfileService', function ($resource, $cookies) {
    var profileService = {};

    this.profileResource = $resource('api/users', {}, {
        'changeLogin': {method: 'PATCH'},
        'changeEmail': {method: 'PATCH'},
        'changePassword': {method: 'PATCH'}
    }, {});

    this.changePasswordResource = $resource('api/users/changepassword', {}, {
        'changePassword': {method: 'POST'}
    }, {});

    var self = this;

    profileService.changeLogin = function (newLogin) {
        return self.profileResource.changeLogin({login: newLogin}).$promise.then(function () {
            $cookies['scentry.auth.default.user'] = newLogin;
        });
    };

    profileService.changeEmail = function (newEmail) {
        return self.profileResource.changeEmail({email: newEmail}).$promise;
    };

    profileService.changePassword = function (currentPassword, newPassword) {
        return self.changePasswordResource.changePassword({currentPassword: currentPassword, newPassword: newPassword}).$promise;
    };

    return profileService;
});