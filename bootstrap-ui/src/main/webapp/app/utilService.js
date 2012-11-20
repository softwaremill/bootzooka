angular.module('utilService', ['ngResource']).
    factory('UtilService', function($resource) {
        var utilService = $resource('/rest/uptime');

        utilService.loadUptime = function(successFunction) {
            return utilService.get(successFunction);
        };

        return utilService;
    });