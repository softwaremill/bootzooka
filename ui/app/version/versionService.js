'use strict';

angular.module('smlBootzooka.version').factory('VersionService', ($http, Version) => {
  function success(response) {
    return new Version(response.data);
  }

  return {
    getVersion: () => $http.get('api/version').then(success)
  };
});
