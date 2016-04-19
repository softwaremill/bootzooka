export default ngModule => {
  ngModule.factory('RegisterService', ($http, $q, FlashService) => {
    return {
      register: user => $http.post('api/users/register', angular.toJson(user)).then(response => {
        if (response.data === 'success') {
          FlashService.set('User registered successfully! Please check your e-mail for confirmation.');
          return response.data;
        } else {
          return $q.reject(response);
        }
      })
    };
  })
}
