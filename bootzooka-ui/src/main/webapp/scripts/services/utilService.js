"use strict";

angular.module("smlBootzooka.maintenance").factory('UtilService', function ($resource) {

    var self = this;
    var dontBlockOnAjaxHeader = { "dontBlockPageOnAjax": "true" };
    var nonArrayGetWithoutBlockOnAjax = { method: "GET", isArray: false, headers: dontBlockOnAjaxHeader };

    self.utilResource = $resource('/rest/uptime', { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    var utilService = {};

    utilService.loadUptime = function (successFunction) {
        return self.utilResource.get(successFunction);
    };

    return utilService;
});
