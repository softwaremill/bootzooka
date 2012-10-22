angular.module('log', ['logService', 'logCounterService', 'utilService']);


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