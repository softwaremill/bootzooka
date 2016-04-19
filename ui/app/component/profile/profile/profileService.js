export default ngModule => {
  ngModule.factory('ProfileService', function ($resource, $cookies) {
    const profileService = {};

    this.profileResource = $resource('api/users', {}, {
      'changeLogin': {method: 'PATCH'},
      'changeEmail': {method: 'PATCH'},
      'changePassword': {method: 'PATCH'}
    }, {});

    this.changePasswordResource = $resource('api/users/changepassword', {}, {
      'changePassword': {method: 'POST'}
    }, {});

    profileService.changeLogin = newLogin =>
      this.profileResource.changeLogin({login: newLogin}).$promise.then(() => {
        $cookies['scentry.auth.default.user'] = newLogin;
      });

    profileService.changeEmail = newEmail =>
      this.profileResource.changeEmail({email: newEmail}).$promise;


    profileService.changePassword = (currentPassword, newPassword) => {
      return this.changePasswordResource.changePassword({
        currentPassword: currentPassword,
        newPassword: newPassword
      }).$promise;
    };

    return profileService;
  })
}
