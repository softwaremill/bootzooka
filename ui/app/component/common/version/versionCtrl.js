export default ngModule => {
  ngModule.controller('VersionCtrl', ($scope, VersionService) => {

    VersionService.getVersion().then(version => {
      $scope.buildSha = version.getBuildSha();
      $scope.buildDate = version.getBuildDate();
    });
  });
};
