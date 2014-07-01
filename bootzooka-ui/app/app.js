"use strict";

angular.module('smlBootzooka.directives', []);
angular.module('smlBootzooka.filters', []);
angular.module('smlBootzooka.maintenance', ['ngResource']);

angular.module('smlBootzooka.profile', ['ngRoute', 'smlBootzooka.maintenance', 'smlBootzooka.session', 'smlBootzooka.directives']).config(function ($routeProvider) {
    $routeProvider.
        when("/login", {controller: 'LoginCtrl', templateUrl: "login/login.html"}).
        when("/register", {controller: 'RegisterCtrl', templateUrl: "register/register.html"}).
        when("/recover-lost-password", {controller: 'PasswordRecoveryCtrl', templateUrl: "password/recover-lost-password.html"}).
        when("/password-reset", {controller: "PasswordRecoveryCtrl", templateUrl: "password/password-reset.html"}).
        when("/profile", {controller: "ProfileCtrl", templateUrl: "profile/profile.html", auth: true}).
        when("/main", {templateUrl: "common/private.html", auth: true}).
        when("/", {templateUrl: "common/public.html"});
});

angular.module('smlBootzooka.session', ['ngCookies', 'ngResource']);

angular.module(
        'smlBootzooka', [
            'smlBootzooka.templates',
            'smlBootzooka.profile',
            'smlBootzooka.session',
            'smlBootzooka.directives', 'ngSanitize', 'ngRoute', 'ajaxthrobber']).config(function ($routeProvider) {
        $routeProvider.
            when("/error404", {templateUrl: "common/errorpages/error404.html"}).
            when("/error500", {templateUrl: "common/errorpages/error500.html"}).
            when("/error", {templateUrl: "common/errorpages/error500.html"}).
            otherwise({redirectTo: "/error404"});
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

    .run(function ($rootScope, UserSessionService, $location) {
        $rootScope.$on('$routeChangeStart', function(ev, next) {
            if(next && next.auth && UserSessionService.isNotLogged()) {
                $location.path('/login');
            }
        });
    })

    .run(function ($rootScope, $timeout, FlashService) {
        $rootScope.$on("$routeChangeSuccess", function () {
            var message = FlashService.get();
            if (angular.isDefined(message)) {
                bootzooka.utils.showInfoMessage(message);
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
            link: function (scope, element) {
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
