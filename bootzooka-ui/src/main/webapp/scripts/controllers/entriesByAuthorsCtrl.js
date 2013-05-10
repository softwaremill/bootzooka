"use strict";

angular.module('smlBootzooka.entries').controller('EntriesByAuthorsCtrl', function EntriesByAuthorsCtrl($scope, $location, $routeParams, EntriesService, UserSessionService) {
    $scope.authorId = $routeParams.authorId;

    $scope.loadAuthors = function () {
        UserSessionService.loadAll(function (data) {
            $scope.authors = data;
        });
    };

    $scope.loadAuthors();

    $scope.loadEntries = function () {
        if (!angular.isUndefined($scope.authorId)) {
            EntriesService.loadAuthoredBy($scope.authorId, function (data) {
                $scope.entries = data;

                if (!$scope.noEntries()) {
                    $scope.author = $scope.entries[0].author;
                }
            });
        }
    };

    $scope.loadEntries();

    $scope.authorSelected = function () {
        return !angular.isUndefined($scope.authorId);
    };

    $scope.showEntriesAuthoredBy = function (author) {
        $location.path('/entries/author/' + author.id);
    };

    $scope.noEntries = function () {
        return angular.isUndefined($scope.entries) || $scope.entries.length == 0;
    };

    $scope.isOwnerOf = function (entry) {
        return UserSessionService.isLogged() && entry.author === UserSessionService.loggedUser.login;
    };
});