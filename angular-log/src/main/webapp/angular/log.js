angular.module('log', ['logService', 'logCounterService', 'utilService']).
    config(function($routeProvider) {

        $routeProvider.
            when('/', {controller:LogsCtrl, templateUrl:'partials/main.html'}).

            when("/login", {controller: LogsCtrl, templateUrl: "partials/login.html"}).

            otherwise({redirectTo:'/'})
    })

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
        $scope.logs = LogService.addNew($scope.entryText, function() {
            $scope.logs = LogService.query();
            $scope.size = LogCounterService.countLogs();
        })

        $scope.entryText = '';
    }

}