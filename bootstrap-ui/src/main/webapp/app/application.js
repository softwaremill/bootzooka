angular.module('smlBootstrap', ['entriesService', 'utilService', 'userSessionService', 'registerService', 'smlBootstrap.services','smlBootstrap.filters', 'smlBootstrap.controllers', 'ngSanitize'])

    .config(function($routeProvider) {

        $routeProvider.
            when('/', {controller: 'EntriesController', templateUrl:'partials/main.html'}).
            when("/entry/:entryId", {controller: 'EntryEditController', templateUrl: "partials/entry.html"}).
            when("/login", {controller: 'LoginController', templateUrl: "partials/login.html"}).
            when("/register", {controller: 'RegisterController', templateUrl: "partials/register.html"}).
            otherwise({redirectTo:'/'})
    })

    .config(['$httpProvider', function($httpProvider) {

        var interceptor = ['$rootScope', '$q', '$location', function($rootScope, $q, $location) {
            function success(response) {
                return response;
            }

            function error(response) {
                // user is not logged in, remove user data from rootScope
                if (response.status === 401) {
                    if($rootScope.loggedUser != null) {
                        $location.path("/login");
                        showInfoMessage("Your session timed out. Please login again.");
                        $rootScope.loggedUser = null;
                    }
                }
                else if(response.status === 403) {
                    console.log("403");
                    // do nothing, user is trying to modify data without privileges
                }
                else {
                    showErrorMessage("Service not responding. Please try again later.");
                }
                return $q.reject(response);
            }

            return function(promise) {
                return promise.then(success, error);
            }

        }];
        $httpProvider.responseInterceptors.push(interceptor);
    }])

    .run(function($rootScope, UserSessionService) {
        UserSessionService.validate();
    });


angular.module('smlBootstrap.filters', []).
    filter('newlines', function () {
        return function (text) {
            return text.replace(/\n/g, '<br/>');
        };
    });