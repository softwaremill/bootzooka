'use strict';

angular.module("smlBootzooka.version").factory('VersionService', function ($http, Version) {

    var versionService = {};

    versionService.getVersion = function () {
        return $http.get('rest/version').then(success);
    };

    function success(response) {
        return new Version(response.data);
    }

    return versionService;
});