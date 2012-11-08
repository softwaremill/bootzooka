angular.module('log', ['logService', 'logCounterService', 'utilService', 'userService', 'logoutService']).
    config(function($routeProvider) {

        $routeProvider.
            when('/', {controller:LogsCtrl, templateUrl:'partials/main.html'}).
            when("/entry/:entryId", {controller: LogsCtrl, templateUrl: "partials/entry.html"}).
            when("/login", {controller: LoginCtrl, templateUrl: "partials/login.html"}).
            otherwise({redirectTo:'/'})
    })

    .config(['$httpProvider', function($httpProvider) {

        var interceptor = ['$rootScope', '$q', function($rootScope, $q) {
            function success(response) {
                return response;
            }

            function error(response) {
                // user is not logged in, remove user data from rootScope
                console.log("response =" + response);
                if (response.status === 401) {
                    showInfoMessage("Your session timed out. Please login again.")
                    $rootScope.loggedUser = null;
                }
                else if(response.status === 403) {
                    console.log("403");
                    // do nothing, user is trying to modify data without privileges
                }
                else {
                    showErrorMessage("Service not responsing. Please try again later.")
                }
                // otherwise
                return $q.reject(response);
            }

            return function(promise) {
                return promise.then(success, error);
            }

        }];
        $httpProvider.responseInterceptors.push(interceptor);
    }])

    .run(function($rootScope, LogoutService) {

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
            LogoutService.logout(new function() {
                $rootScope.loggedUser = null;
                showInfoMessage("Logged out successfully");
            });
        }
    })

    .run(function($rootScope) {
         //todo autologin basing on cookies should be here
    });