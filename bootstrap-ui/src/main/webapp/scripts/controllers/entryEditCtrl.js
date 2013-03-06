"use strict";

angular.module('smlBootstrap.entries').controller('EntryEditCtrl', function EntryEditCtrl($scope, EntriesService, $routeParams, $location, UserSessionService) {

    $scope.logId = $routeParams.entryId;
    $scope.log = {};

    EntriesService.load($scope.logId, function (data) {
        $scope.log = data;
    });

    $scope.updateEntry = function () {
        EntriesService.update($scope.log);
        $location.path("");
    };

    $scope.cancelEdit = function () {
        $location.path("");
    };

    $scope.isOwnerOfEntry = function () {
        return $scope.log.author === UserSessionService.loggedUser.login;
    };

    $scope.isLogged = function () {
        return UserSessionService.isLogged();
    };

    $scope.isNotLogged = function () {
        return UserSessionService.isNotLogged();
    };
});