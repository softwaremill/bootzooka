'use strict';

angular.module('smlBootzooka.version').controller('VersionCtrl', ($scope, VersionService) => {

  VersionService.getVersion().then(version => {
    $scope.buildSha = version.getBuildSha();
    $scope.buildDate = version.getBuildDate();
  });
});
