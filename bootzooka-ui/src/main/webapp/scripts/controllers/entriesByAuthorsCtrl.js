"use strict";

angular.module('smlBootzooka.entries').controller('EntriesByAuthorsCtrl', function EntriesByAuthorsCtrl($scope, $location, $routeParams, EntriesByAuthorsService, UserSessionService) {
    function loadAuthors() {
        EntriesByAuthorsService.getAllAuthors(function (data) {
            $scope.authors = data;
        });
    }

    loadAuthors();

    function loadEntries() {
        if (!angular.isUndefined($routeParams.authorId)) {
            EntriesByAuthorsService.loadAuthoredBy($routeParams.authorId, function (data) {
                $scope.entries = {
                    items: data
                };

                if (!$scope.noEntries()) {
                    $scope.entries.author = $scope.entries.items[0].author;
                }
            });
        }
    }

    loadEntries();

    $scope.authorSelected = function () {
        return !angular.isUndefined($routeParams.authorId);
    };

    $scope.isCurrentAuthor = function (author) {
        return author.id === $routeParams.authorId;
    };

    $scope.showEntriesAuthoredBy = function (author) {
        $location.path('/entries/author/' + author.id);
    };

    $scope.noEntries = function () {
        return angular.isUndefined($scope.entries) || angular.isUndefined($scope.entries.items)
            || $scope.entries.items.length === 0;
    };

    $scope.isOwnerOf = function (entry) {
        return UserSessionService.isLogged() && entry.author === UserSessionService.loggedUser.login;
    };
});