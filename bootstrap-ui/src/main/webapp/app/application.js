angular.module('smlBootstrap', ['smlBootstrap.services','smlBootstrap.filters', 'smlBootstrap.controllers', 'ngSanitize'])

    .config(function($routeProvider) {

        $routeProvider.
            when('/', {controller: 'EntriesController', templateUrl:'partials/main.html'}).
            when("/entry/:entryId", {controller: 'EntryEditController', templateUrl: "partials/entry.html"}).
            when("/login", {controller: 'LoginController', templateUrl: "partials/login.html"}).
            when("/register", {controller: 'RegisterController', templateUrl: "partials/register.html"}).
            otherwise({redirectTo:'/'})
    })

    .config(['$httpProvider', function($httpProvider) {

        var interceptor = ['$q', '$location', 'FlashService', '$injector', function($q, $location, FlashService, $injector) {
            function success(response) {
                return response;
            }

            function error(response) {
                if (response.status === 401) { // user is not logged in
                    var UserSessionService = $injector.get('UserSessionService'); // uses $injector to avoid circular dependency
                    if(UserSessionService.isLogged()) {
                        UserSessionService.logout(); // Http session expired / logged out - logout on Angular layer
                        FlashService.set('Your session timed out. Please login again.');
                        $location.path("/login");
                    }
                }
                else if(response.status === 403) {
                    console.log(response.data);
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
    })

    .run(function($rootScope, $timeout, FlashService) {
        $rootScope.$on("$routeChangeSuccess", function () {
            var message = FlashService.get();
            if (angular.isDefined(message)) {
                showInfoMessage(message);
            }
        });
    });


angular.module('smlBootstrap.filters', []).
    filter('newlines', function () {
        return function (text) {
            return text.replace(/\n/g, '<br/>');
        };
    });