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
//        console.log("scope.size 0 = " + $scope.size.value)

        LogService.addNew($scope.entryText)

        $scope.entryText = '';
        $scope.logs = LogService.query();
        $scope.size = LogCounterService.countLogs();
//        console.log("scope.size = " + $scope.size.value)
    }

}