angular.module('log', ['logService', 'logCounterService', 'utilService']).
    config(function($routeProvider) {

        $routeProvider.
            when('/', {controller:LogsCtrl, templateUrl:'partials/main.html'}).
            when("/entry/:entryId", {controller: LogsCtrl, templateUrl: "partials/entry.html"}).
            otherwise({redirectTo:'/'})
    });

function UptimeCtrl($scope, UtilService) {
    $scope.uptime = UtilService.loadUptime();
}


function LogsCtrl($scope, LogService, LogCounterService) {

    var self = this;

    $scope.logs = LogService.query();
    $scope.size = LogCounterService.countLogs();
    $scope.entryText = '';

    $scope.addEntry = function() {
        LogService.addNew($scope.entryText, function() {
            self.reloadEntries();
            self.resetForm();
        });
    };

    $scope.deleteEntry = function(logEntryId) {
        LogService.deleteEntry(logEntryId, function() {
            self.reloadEntries();
        })
    };

    $scope.noEntries = function() {
        return 0 === $scope.size.value;
    };

    this.reloadEntries = function() {
        $scope.logs = LogService.query();
        $scope.size = LogCounterService.countLogs();
    }

    this.resetForm = function() {
        $scope.entryText = '';
        $scope.myForm.$pristine = true;
    }
}


function LogEditCtrl($scope, LogService, $routeParams, $location) {

    $scope.logId = $routeParams.entryId;
    $scope.log = LogService.load($scope.logId);

    $scope.updateEntry = function() {
        LogService.update($scope.log);
        $location.path("");
    }
}
