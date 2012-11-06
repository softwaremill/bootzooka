
function UptimeCtrl($scope, UtilService) {
    $scope.uptime = UtilService.loadUptime();
}

function LogsCtrl($scope, LogService, LogCounterService) {

    var self = this;

    $scope.entryText = '';
    $scope.message = '';

    this.reloadEntries = function() {
        $scope.logs = LogService.query();
        $scope.size = LogCounterService.countLogs();
    }

    this.reloadEntries();


    $scope.addEntry = function() {
        LogService.addNew($scope.entryText, function() {
            self.reloadEntries();
            $scope.entryText = '';
            $scope.myForm.$pristine = true;
            showMessage("Message posted");
        });
    };

    $scope.deleteEntry = function(logEntryId) {
        LogService.deleteEntry(logEntryId, function() {
            self.reloadEntries();
            showMessage("Message removed");
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
