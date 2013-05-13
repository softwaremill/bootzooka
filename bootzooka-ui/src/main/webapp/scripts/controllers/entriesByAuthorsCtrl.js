"use strict";

angular.module('smlBootzooka.entries').controller('EntriesByAuthorsCtrl', function EntriesByAuthorsCtrl($scope, $location, $routeParams, EntriesByAuthorsService, UserSessionService) {
    $scope.authorId = $routeParams.authorId;

    function loadAuthors() {
        EntriesByAuthorsService.getAllAuthors(function (data) {
            $scope.authors = data;
        });
    }

    loadAuthors();

    function loadEntries() {
        if (!angular.isUndefined($scope.authorId)) {
            EntriesByAuthorsService.loadAuthoredBy($scope.authorId, function (data) {
                $scope.entries = data;

                if (!$scope.noEntries()) {
                    $scope.author = $scope.entries[0].author;
                }
            });
        }
    }

    loadEntries();

    $scope.authorSelected = function () {
        return !angular.isUndefined($scope.authorId);
    };

    $scope.isCurrentAuthor = function (author) {
        return author.id === $scope.authorId;
    };

    $scope.showEntriesAuthoredBy = function (author) {
        $location.path('/entries/author/' + author.id);
    };

    $scope.noEntries = function () {
        return angular.isUndefined($scope.entries) || $scope.entries.length === 0;
    };

    $scope.isOwnerOf = function (entry) {
        return UserSessionService.isLogged() && entry.author === UserSessionService.loggedUser.login;
    };
});