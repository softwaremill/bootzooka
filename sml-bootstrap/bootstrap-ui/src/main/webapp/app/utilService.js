angular.module('utilService', ['ngResource']).
    factory('UtilService', function($resource) {
        var UtilService = $resource('/rest/uptime');

        UtilService.loadUptime = function(cb) {
            return UtilService.get();
        }

        return UtilService;
    });