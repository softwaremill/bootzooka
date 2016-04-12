export default ngModule => {
  ngModule.factory('PasswordResetService', ($q, $http) => {
    let passwordresetService = {};

    passwordresetService.beginResetProcess = login =>
      $http.post('api/passwordreset', {login: login}).then(response => {
        if (response.data !== 'success') {
          return $q.reject(response.data);
        }
        return response;
      });

    passwordresetService.changePassword = (code, password) =>
      $http.post('api/passwordreset/' + code, {code: code, password: password});

    return passwordresetService;
  })
}
