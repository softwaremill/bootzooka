"use strict";
angular.module('smlBootstrap.directives', []);
angular.module('smlBootstrap.filters', []);
angular.module('smlBootstrap.maintenance', ['ngResource']);

angular.module('smlBootstrap.profile', ['smlBootstrap.maintenance', 'smlBootstrap.session', 'smlBootstrap.directives']).config(function ($routeProvider) {
    $routeProvider.
        when("/login", {controller: 'LoginCtrl', templateUrl: "views/login.html"}).
        when("/register", {controller: 'RegisterCtrl', templateUrl: "views/register.html"}).
        when("/recover-lost-password", {controller: 'PasswordRecoveryCtrl', templateUrl: "views/recover-lost-password.html"}).
        when("/password-reset", {controller: "PasswordRecoveryCtrl", templateUrl: "views/password-reset.html"}).
        when("/profile", {controller: "ProfileCtrl", templateUrl: "views/secured/profile.html"});
});

angular.module('smlBootstrap.entries', ['smlBootstrap.session', 'smlBootstrap.filters']).config(function ($routeProvider) {
    $routeProvider.
        when('/', {controller: 'EntriesCtrl', templateUrl: 'views/main.html'}).
        when("/entry/:entryId", {controller: 'EntryEditCtrl', templateUrl: "views/entry.html"});
});

angular.module('smlBootstrap.session', ['ngCookies', 'ngResource']);

angular.module(
        'smlBootstrap', [
            'smlBootstrap.filters',
            'smlBootstrap.profile',
            'smlBootstrap.entries',
            'smlBootstrap.maintenance',
            'smlBootstrap.session',
            'smlBootstrap.directives', 'ngSanitize', 'ajaxthrobber']).config(function ($routeProvider) {
        $routeProvider.
            when("/error404", {controller: 'EntriesCtrl', templateUrl: "views/errorpages/error404.html"}).
            when("/error500", {controller: 'EntriesCtrl', templateUrl: "views/errorpages/error500.html"}).
            when("/error", {controller: 'EntriesCtrl', templateUrl: "views/errorpages/error500.html"}).
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
    })

    .run(function ($rootScope, UserSessionService, $location) {
        $rootScope.$on("$routeChangeStart", function (event, next, current) {
            if (!UserSessionService.isLogged() && (typeof next.templateUrl !== "undefined") && next.templateUrl.indexOf("/secured/") > -1) {
                $location.search("page", $location.url()).path("/login");
            } else if (UserSessionService.isLogged() && next.templateUrl === "partials/login.html") {
                $location.path("/");
            }
        });
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
