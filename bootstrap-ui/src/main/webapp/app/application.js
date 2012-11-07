angular.module('log', ['logService', 'logCounterService', 'utilService', 'userService']).
    config(function($routeProvider) {

        $routeProvider.
            when('/', {controller:LogsCtrl, templateUrl:'partials/main.html'}).
            when("/entry/:entryId", {controller: LogsCtrl, templateUrl: "partials/entry.html"}).
            when("/login", {controller: LoginCtrl, templateUrl: "partials/login.html"}).
            otherwise({redirectTo:'/'})
    })

    .run(function($rootScope) {

        $rootScope.loggedUser = null;

        $rootScope.isLogged = function() {
            return $rootScope.loggedUser != null;
        }

        $rootScope.isNotLogged = function() {
            return $rootScope.loggedUser == null;
        }

        $rootScope.logUser = function(user) {
            $rootScope.loggedUser = user;

        }

        $rootScope.logout = function() {
            $rootScope.loggedUser = null;
            showInfoMessage("Logged out successfully");
        }
    })

    .run(function($rootScope) {
         //todo autologin basing on cookies should be here
    });