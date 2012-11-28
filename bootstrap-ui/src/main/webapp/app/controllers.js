angular.module('smlBootstrap.controllers', [])

        .controller('UptimeController', function UptimeController($scope, UtilService) {
            UtilService.loadUptime(function (data) {
                $scope.uptime = data.value;
            });
        })

        .controller('EntriesController', function EntriesController($scope, $timeout, EntriesService, UserSessionService, FlashService) {

            var self = this;

            $scope.entryText = '';
            $scope.message = '';
            $scope.size = 0;

            this.reloadEntries = function () {
                EntriesService.count(function (data) {
                    $scope.size = data.value;
                });

                EntriesService.loadAll(function (data) {
                    $scope.logs = data;
                });
            };

            this.reloadEntries();

            this.reloadEventId = $timeout(function reloadEntriesLoop() {
                self.reloadEntries();
                self.reloadEventId = $timeout(reloadEntriesLoop, 3000);
            }, 3000);

            $scope.$on("$routeChangeStart", function () {
                $timeout.cancel(self.reloadEventId);
            });

            $scope.$on("$routeChangeSuccess", function () {
                var message = FlashService.get();
                if (angular.isDefined(message)) {
                    showInfoMessage(message);
                }
            });

            $scope.addEntry = function () {
                EntriesService.addNew($scope.entryText, function () {
                    self.reloadEntries();
                    $scope.entryText = '';
                    $scope.myForm.$pristine = true;
                    showInfoMessage("Message posted");
                });
            };

            $scope.deleteEntry = function (logEntryId) {
                EntriesService.deleteEntry(logEntryId, function () {
                    self.reloadEntries();
                    showInfoMessage("Message removed");
                })
            };

            $scope.noEntries = function () {
                return 0 === $scope.size;
            };

            $scope.isOwnerOf = function (entry) {
                return UserSessionService.isLogged() && entry.author === UserSessionService.loggedUser.login;
            };

            $scope.getLoggedUserName = function () {
                return UserSessionService.getLoggedUserName();
            };

            $scope.isLogged = function () {
                return UserSessionService.isLogged()
            };

            $scope.isNotLogged = function () {
                return UserSessionService.isNotLogged()
            };

            $scope.logout = function () {
                UserSessionService.logout();
                showInfoMessage("Logged out successfully");
            };
        })

        .controller('EntryEditController', function EntryEditController($scope, EntriesService, $routeParams, $location, UserSessionService) {

            $scope.logId = $routeParams.entryId;
            $scope.log = new Object();

            EntriesService.load($scope.logId, function (data) {
                $scope.log = data;
            });

            $scope.updateEntry = function () {
                EntriesService.update($scope.log);
                $location.path("");
            };

            $scope.isOwnerOfEntry = function () {
                return $scope.log.author === UserSessionService.loggedUser.login;
            };

            $scope.isLogged = function () {
                return UserSessionService.isLogged()
            };

            $scope.isNotLogged = function () {
                return UserSessionService.isNotLogged()
            };
        })

        .controller('RegisterController', function RegisterController($scope, RegisterService, $location) {

            var self = this;

            $scope.user = {};
            $scope.user.login = '';
            $scope.user.password = '';
            $scope.user.email = '';
            $scope.user.repeatPassword = '';

            $scope.register = function () {
                $scope.registerForm.login.$dirty = true;
                $scope.registerForm.password.$dirty = true;
                $scope.registerForm.email.$dirty = true;
                $scope.registerForm.repeatPassword.$dirty = true;

                if ($scope.registerForm.$valid) {
                    var jsonUser = {}; // create dedicated object to pass only specific fields
                    jsonUser.login = $scope.user.login;
                    jsonUser.email = $scope.user.email;
                    jsonUser.password = $scope.user.password;

                    RegisterService.register(jsonUser, self.registerOk, self.registerFailed)
                }
            };

            $scope.checkPassword = function() {
                $scope.registerForm.repeatPassword.$error.dontMatch = $scope.user.password != $scope.user.repeatPassword;
            };

            this.registerOk = function () {
                $location.path("");
            };

            this.registerFailed = function (message) {
                showErrorMessage(message)
            }

        })

        .controller('LoginController', function LoginController($scope, UserSessionService, $location) {

            var self = this;

            $scope.user = new Object();
            $scope.user.login = '';
            $scope.user.password = '';
            $scope.user.rememberme = false;

            $scope.login = function () {
                // set dirty to show error messages on empty fields when submit is clicked
                $scope.loginForm.login.$dirty = true;
                $scope.loginForm.password.$dirty = true;

                if ($scope.loginForm.$invalid === false) {
                    UserSessionService.login($scope.user, self.loginOk, self.loginFailed);
                }
            };


            this.loginOk = function () {
                $location.path("");
            };

            this.loginFailed = function () {
                showErrorMessage("Invalid login and/or password.")
            }
        });
