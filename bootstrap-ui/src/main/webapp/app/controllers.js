
function UptimeController($scope, UtilService) {
    $scope.uptime = UtilService.loadUptime();
}

function EntriesController($scope, EntriesService, EntriesCounterService) {

    var self = this;

    $scope.entryText = '';
    $scope.message = '';

    this.reloadEntries = function() {
        $scope.logs = EntriesService.query();
        $scope.size = EntriesCounterService.countLogs();
    }

    this.reloadEntries();

    self.refresherId = setInterval(function() {
        self.reloadEntries();
    }, 4000);

    $scope.$on("$routeChangeStart", function(next, current) {
        clearInterval(self.refresherId);
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
    $scope.log = EntriesService.load($scope.logId);

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
