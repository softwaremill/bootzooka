angular.module('utilService', ['ngResource']).
    factory('UtilService', function($resource) {
        var UtilService = $resource('/uptime');

        UtilService.loadUptime = function(cb) {
            return UtilService.get();
        }

        return UtilService;
    });