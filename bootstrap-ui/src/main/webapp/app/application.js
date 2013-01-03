"use strict";

angular.module('smlBootstrap', ['smlBootstrap.services', 'smlBootstrap.filters', 'smlBootstrap.controllers', 'ngSanitize', 'ajaxthrobber'])

    .config(function ($routeProvider) {

        $routeProvider.
            when('/', {controller: 'EntriesController', templateUrl: 'partials/main.html'}).
            when("/entry/:entryId", {controller: 'EntryEditController', templateUrl: "partials/entry.html"}).
            when("/login", {controller: 'LoginController', templateUrl: "partials/login.html"}).
            when("/register", {controller: 'RegisterController', templateUrl: "partials/register.html"}).
            when("/error404", {controller: 'EntriesController', templateUrl: "partials/errorpages/error404.html"}).
            when("/error500", {controller: 'EntriesController', templateUrl: "partials/errorpages/error500.html"}).
            when("/error", {controller: 'EntriesController', templateUrl: "partials/errorpages/error500.html"}).
            otherwise({redirectTo: '/error404'});
    })

    .config(['$httpProvider', function ($httpProvider) {

        var interceptor = ['$q', '$location', 'FlashService', '$injector', function ($q, $location, FlashService, $injector) {
            function success(response) {
                return response;
            }

            function error(response) {
                if (response.status === 401) { // user is not logged in
                    var UserSessionService = $injector.get('UserSessionService'); // uses $injector to avoid circular dependency
                    if (UserSessionService.isLogged()) {
                        UserSessionService.logout(); // Http session expired / logged out - logout on Angular layer
                        FlashService.set('Your session timed out. Please login again.');
                        $location.path("/login");
                    }
                } else if (response.status === 403) {
                    console.log(response.data);
                    // do nothing, user is trying to modify data without privileges
                } else if (response.status === 404) {
                    $location.path("/error404");
                } else if (response.status === 500) {
                    $location.path("/error500");
                } else {
                    $location.path("/error");
                }
                return $q.reject(response);
            }

            return function (promise) {
                return promise.then(success, error);
            };

        }];
        $httpProvider.responseInterceptors.push(interceptor);
    }])

    .run(function ($rootScope, UserSessionService) {
        UserSessionService.validate();
    })

    .run(function ($rootScope, $timeout, FlashService) {
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


angular.module("ajaxthrobber", [])
    .config(["$httpProvider", function ($httpProvider) {
        var stopAjaxInterceptor;
        stopAjaxInterceptor = ["$rootScope", "$q", function ($rootScope, $q) {
            var error, stopAjax, success, wasPageBlocked;

            stopAjax = function (responseConfig) {
                if (wasPageBlocked(responseConfig)) {
                    return $rootScope.inProgressAjaxCount--;
                }
            };

            wasPageBlocked = function (responseConfig) {
                var nonBlocking;
                if (typeof responseConfig === "object" && typeof responseConfig.headers === "object") {
                    nonBlocking = responseConfig.headers.dontBlockPageOnAjax;
                }
                if (nonBlocking) {
                    return !nonBlocking;
                } else {
                    return true;
                }
            };
            success = function (response) {
                stopAjax(response.config);
                return response;
            };
            error = function (response) {
                stopAjax(response.config);
                return $q.reject(response);
            };
            return function (promise) {
                return promise.then(success, error);
            };
        }];
        return $httpProvider.responseInterceptors.push(stopAjaxInterceptor);
    }])
    .run(["$rootScope", "$http", function ($rootScope, $http) {
        var startAjaxTransformer;
        startAjaxTransformer = function (data, headersGetter) {

            if (!headersGetter().dontBlockPageOnAjax) {
                $rootScope.inProgressAjaxCount++;
            }
            return data;
        };
        $rootScope.inProgressAjaxCount = 0;
        return $http.defaults.transformRequest.push(startAjaxTransformer);
    }])
    .directive("ajaxthrobber", function () {
        return {
            restrict: "E",
            link: function (scope, element, attrs) {
                return scope.$watch("inProgressAjaxCount", function (count) {
                    if (count === 1) {
                        return $.blockUI({
                            message: element
                        });
                    } else if (count === 0) {
                        return $.unblockUI();
                    }
                });
            }
        };
    });
