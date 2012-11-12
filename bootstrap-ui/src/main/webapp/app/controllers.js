
function UptimeController($scope, UtilService) {
    UtilService.loadUptime(function(data) {
        $scope.uptime = data.value;
    });
}

function EntriesController($scope, $timeout, EntriesService, EntriesCounterService) {

    var self = this;

    $scope.entryText = '';
    $scope.message = '';
    $scope.size = 0;

    this.reloadEntries = function() {
        EntriesCounterService.countLogs(function(data) {
            $scope.size = data.value;
        });

        EntriesService.loadAll(function(data) {
            $scope.logs = data;
        });
    }

    this.reloadEntries();

    this.reloadEventId = $timeout(function reloadEntriesLoop() {
        self.reloadEntries();
        console.log("Reloading...")
        self.reloadEventId = $timeout(reloadEntriesLoop, 3000);
    }, 3000);

    $scope.$on("$routeChangeStart", function(next, current) {
        console.log("cancelling");
        $timeout.cancel(self.reloadEventId);
    });

    $scope.addEntry = function() {
        EntriesService.addNew($scope.entryText, function () {
            self.reloadEntries();
            $scope.entryText = '';
            $scope.myForm.$pristine = true;
            showInfoMessage("Message posted");
        });
    };

    $scope.deleteEntry = function(logEntryId) {
        EntriesService.deleteEntry(logEntryId, function() {
            self.reloadEntries();
            showInfoMessage("Message removed");
        })
    };

    $scope.noEntries = function() {
        return 0 === $scope.size.value;
    };


    $scope.isOwnerOf = function(entry) {
        return $scope.isLogged() && entry.author === $scope.loggedUser.login;
    }
}


function EntryEditController($scope, EntriesService, $routeParams, $location) {

    $scope.logId = $routeParams.entryId;
    $scope.log = new Object();

    EntriesService.load($scope.logId, function(data) {
        $scope.log = data;
    });

    $scope.updateEntry = function() {
        EntriesService.update($scope.log);
        $location.path("");
    }

    $scope.isOwnerOfEntry = function() {
        var isOwner = $scope.log.author === $scope.loggedUser.login;
        return isOwner;
    }
}


function LoginController($scope, UserService, $location) {

    var self = this;

    $scope.user = new Object();
    $scope.user.login = '';
    $scope.user.password = '';
    $scope.user.rememberme = false;

    $scope.login = function() {
        // set dirty to show error messages on empty fields when submit is clicked
        $scope.loginForm.login.$dirty = true;
        $scope.loginForm.password.$dirty = true;

        if($scope.loginForm.$invalid === false) {
            UserService.loginUser($scope.user, self.loginOk, self.loginFailed);
        }
    }


    this.loginOk = function(data) {
        $scope.logUser(data.value);
        $location.path("");
    }

    this.loginFailed = function(data) {
        showErrorMessage("Invalid login and/or password.")
    }
}
