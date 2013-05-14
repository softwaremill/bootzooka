"use strict";

angular.module("smlBootzooka.entries").factory('EntriesByAuthorsService', function ($resource) {

    var self = this;

    var allAuthors = undefined;

    self.allUsersResource = $resource('/rest/users/all', { }, {
        get: {method: 'GET', isArray: true}
    });

    self.entriesByAuthorResource = $resource('/rest/entries/author/:authorId', { }, {
        get: {method: 'GET', isArray: true}
    });

    var entriesByAuthorsService = {};

    entriesByAuthorsService.getAllAuthors = function (successFunction) {
        if (angular.isUndefined(allAuthors)) {
            self.allUsersResource.get(null, function (data) {
                allAuthors = data;
                successFunction(allAuthors)
            });
        } else {
            successFunction(allAuthors)
        }
    };

    entriesByAuthorsService.loadAuthoredBy = function (authorId, successFunction) {
        self.entriesByAuthorResource.get({authorId: authorId}, successFunction);
    };

    return entriesByAuthorsService;
});