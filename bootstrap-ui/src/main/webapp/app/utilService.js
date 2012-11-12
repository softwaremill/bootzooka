angular.module('utilService', ['ngResource']).
    factory('UtilService', function($resource) {
        var UtilService = $resource('/rest/uptime');

        UtilService.loadUptime = function(successFunction) {
            return UtilService.get(successFunction);
        }

        return UtilService;
    });