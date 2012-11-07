
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
            showInfoMessage("Message posted");
        });
    };

    $scope.deleteEntry = function(logEntryId) {
        LogService.deleteEntry(logEntryId, function() {
            self.reloadEntries();
            showInfoMessage("Message removed");
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


function LoginCtrl($scope, UserService, $location) {

    var self = this;

    $scope.user = new Object();
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.rememberme = false;

    $scope.loggedUser = new Object();

    $scope.login = function() {
        if($scope.loginForm.$invalid === false) {
            console.log("Submitting");
            $scope.loggedUser = UserService.loginUser($scope.user, self.loginOk, self.loginFailed);
        }
    }


    this.loginOk = function(data) {
        console.log("Login ok");
        console.log("data = " + data.value);
    }

    this.loginFailed = function(data) {
        showErrorMessage("Invalid login and/or password.")
    }
}
