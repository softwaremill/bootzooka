export default ngModule => {
  ngModule.factory('VersionService', ($http, Version) => {
    function success(response) {
      return new Version(response.data);
    }

    return {
      getVersion: () => $http.get('api/version').then(success)
    };
  });
};
