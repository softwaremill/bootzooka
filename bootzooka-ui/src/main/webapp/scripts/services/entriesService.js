"use strict";

angular.module("smlBootzooka.entries").factory('EntriesService', function ($resource) {

    var dontBlockOnAjaxHeader = { "dontBlockPageOnAjax": "true" };
    var nonArrayGetWithoutBlockOnAjax = { method: "GET", isArray: false, headers: dontBlockOnAjaxHeader };

    var self = this;

    self.entriesCrudResource = $resource('rest/entries/:id', { }, {
        insert: { method: "PUT"},
        query: nonArrayGetWithoutBlockOnAjax
    });

    self.counterResource = $resource("/rest/entries/count", { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    self.newEntriesCounterResource = $resource("/rest/entries/count-newer/:time", { }, {
        get: nonArrayGetWithoutBlockOnAjax
    });

    self.entriesByAuthorResource = $resource('/rest/entries/author/:authorId', { }, {
        get: {method: 'GET', isArray: true}
    });

    var entriesService = {};

    entriesService.loadAll = function (successFunction) {
        self.entriesCrudResource.query(null, successFunction);
    };

    entriesService.addNew = function (entryText, successFunction) {
        var json = {};
        json.text = entryText;
        self.entriesCrudResource.save(angular.toJson(json), successFunction);
    };

    entriesService.load = function (logObjectId, successFunction) {
        self.entriesCrudResource.get({id: logObjectId}, successFunction);
    };

    entriesService.update = function (logObject) {
        var json = {};
        json.text = logObject.text;
        json.id = logObject.id;
        self.entriesCrudResource.insert(angular.toJson(json));
    };

    entriesService.deleteEntry = function (logObjectId, successFunction) {
        self.entriesCrudResource.remove({id: logObjectId}, successFunction);
    };

    entriesService.count = function (successFunction) {
        self.counterResource.get(successFunction);
    };

    entriesService.countNewEntries = function (timestamp, successFunction) {
        self.newEntriesCounterResource.get({time: timestamp}, successFunction);
    };

    entriesService.loadAuthoredBy = function (authorId, successFunction) {
        self.entriesByAuthorResource.get({authorId: authorId}, successFunction);
    };

    return entriesService;
});
