angular.module('log', ['logService', 'logCounterService', 'utilService', 'userService']).
    config(function($routeProvider) {

        $routeProvider.
            when('/', {controller:LogsCtrl, templateUrl:'partials/main.html'}).
            when("/login", {controller: UserCtrl, templateUrl: "partials/login.html"}).
            when("/entry/:entryId", {controller: LogsCtrl, templateUrl: "partials/entry.html"}).
            otherwise({redirectTo:'/'})
    });

function UptimeCtrl($scope, UtilService) {
    var uptime = UtilService.loadUptime();
    $scope.uptime = uptime;
}


function LogsCtrl($scope, LogService, LogCounterService) {

    var logs = LogService.query();
    $scope.logs = logs;
    $scope.size = LogCounterService.countLogs();
    $scope.entryText = '';

    $scope.addEntry = function() {
        LogService.addNew($scope.entryText, function() {
            $scope.logs = LogService.query();
            $scope.size = LogCounterService.countLogs();
        });

        $scope.entryText = '';
    };

    $scope.deleteEntry = function(logEntryId) {
        LogService.deleteEntry(logEntryId, function() {
            $scope.logs = LogService.query();
            $scope.size = LogCounterService.countLogs();
        })
    };

    $scope.noEntries = function() {
        return 0 === $scope.size.value;
    };
}


function LogEditCtrl($scope, LogService, $routeParams, $location) {

    $scope.logId = $routeParams.entryId;

    $scope.log = LogService.load($scope.logId);

    $scope.updateEntry = function() {
        LogService.update($scope.log);
        $location.path("");
    }
}


function UserCtrl($scope, UserService) {
    console.log("UserCtrl ready!")
}