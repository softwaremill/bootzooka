"use strict";

angular.module("smlBootzooka.entries").factory('EntriesByAuthorsService', function ($resource) {

    var allAuthors = undefined;

    var allUsersResource = $resource('/rest/users/all', { }, {
        get: {method: 'GET', isArray: true}
    });

    var entriesByAuthorResource = $resource('/rest/entries/author/:authorId', { }, {
        get: {method: 'GET', isArray: true}
    });

    var entriesByAuthorsService = {};

    entriesByAuthorsService.getAllAuthors = function (successFunction) {
        if (angular.isUndefined(allAuthors)) {
            allUsersResource.get(null, function (data) {
                allAuthors = data;
                successFunction(allAuthors)
            });
        } else {
            successFunction(allAuthors)
        }
    };

    entriesByAuthorsService.loadAuthoredBy = function (authorId, successFunction) {
        entriesByAuthorResource.get({authorId: authorId}, successFunction);
    };

    return entriesByAuthorsService;
});