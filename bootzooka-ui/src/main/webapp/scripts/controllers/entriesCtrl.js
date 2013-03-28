"use strict";

angular.module('smlBootzooka.entries').controller('EntriesCtrl', function EntriesCtrl($scope, $timeout, $window, EntriesService, UserSessionService, $location) {

    var self = this;

    $scope.entryText = '';
    $scope.message = '';
    $scope.size = 0;
    $scope.data = {};
    $scope.lastLoadedTimestamp = 0;
    $scope.numberOfNewEntries = 0;

    $scope.reloadEntries = function () {
        $scope.numberOfNewEntries = 0;
        EntriesService.count(function (data) {
            $scope.size = data.value;
        });

        EntriesService.loadAll(function (data) {
            $scope.logs = data.entries;
            $scope.lastLoadedTimestamp = data.timestamp;
        });
    };

    $scope.reloadEntries();

    this.checkForNewEntries = function () {
        EntriesService.countNewEntries($scope.lastLoadedTimestamp, function (data) {
            $scope.numberOfNewEntries = data.value;
        });
    };

    var checkForNewEntriesEvent = $timeout(function checkForNewEntriesLoop() {
        self.checkForNewEntries();
        checkForNewEntriesEvent = $timeout(checkForNewEntriesLoop, 5000);
    }, 5000);

    $scope.$on("$routeChangeStart", function () {
        $timeout.cancel(checkForNewEntriesEvent);
    });

    var addEntryInProgress = false;

    $scope.addEntry = function () {
        if (addEntryInProgress === false) {
            addEntryInProgress = true;
            var newEntryText = $scope.entryText;
            $scope.entryText = '';
            EntriesService.addNew(newEntryText, function () {
                $scope.reloadEntries();
                $scope.myForm.$pristine = true;
                showInfoMessage("Message posted");
                addEntryInProgress = false;
            });
        }
        $('[name="message"]').focus();
    };

    $scope.deleteEntry = function (logEntryId) {
        EntriesService.deleteEntry(logEntryId, function () {
            $scope.reloadEntries();
            showInfoMessage("Message removed");
        });
    };

    $scope.noEntries = function () {
        return 0 === $scope.size;
    };

    $scope.isOwnerOf = function (entry) {
        return UserSessionService.isLogged() && entry.author === UserSessionService.loggedUser.login;
    };

    $scope.isLogged = function () {
        return UserSessionService.isLogged();
    };
});