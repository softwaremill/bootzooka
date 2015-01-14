"use strict";

angular.module('smlBootzooka.directives', []);
angular.module('smlBootzooka.filters', []);
angular.module('smlBootzooka.maintenance', ['ngResource']);
angular.module('smlBootzooka.notifications', []);

angular.module('smlBootzooka.profile', ['ui.router', 'smlBootzooka.maintenance', 'smlBootzooka.session', 'smlBootzooka.directives', 'smlBootzooka.notifications'])
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.when('', '/');

        $stateProvider
            .state('login', {
                url: '/login',
                controller: 'LoginCtrl',
                templateUrl: "login/login.html",
                params: {
                    page: null
                }
            })
            .state('register', {
                url: '/register',
                controller: 'RegisterCtrl',
                templateUrl: "register/register.html"
            })
            .state('recover-lost-password', {
                url: '/recover-lost-password',
                controller: 'PasswordRecoveryCtrl',
                templateUrl: "password/recover-lost-password.html"
            })
            .state('password-reset', {
                url: '/password-reset?code',
                controller: "PasswordRecoveryCtrl",
                templateUrl: "password/password-reset.html"
            })
            .state('profile', {
                url: '/profile',
                controller: "ProfileCtrl",
                templateUrl: "profile/profile.html",
                data: {
                    auth: true
                }
            })
            .state('main', {
                url: '/main',
                templateUrl: "common/private.html",
                data: {
                    auth: true
                }
            })
            .state('home', {
                url: '/',
                templateUrl: "common/public.html"
            });
    });

angular.module('smlBootzooka.session', ['ngCookies', 'ngResource']);

angular.module(
        'smlBootzooka', [
            'smlBootzooka.templates',
            'smlBootzooka.profile',
            'smlBootzooka.session',
            'smlBootzooka.directives',
            'smlBootzooka.notifications', 'ngSanitize', 'ui.router', 'ajaxthrobber'])
    .config(function ($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise('/error404');

        $stateProvider
            .state('error404', {
                url: '/error404',
                templateUrl: 'common/errorpages/error404.html'
            })
            .state('error500', {
                url: '/error500',
                templateUrl: 'common/errorpages/error500.html'
            })
            .state('error', {
                url: '/error',
                templateUrl: 'common/errorpages/error500.html'
            });
    })
    .config(['$httpProvider', function ($httpProvider) {
        var interceptor = ['$q', 'FlashService', '$injector', function ($q, FlashService, $injector) {

            function redirectToState(stateName) {
                // Because $httpProvider is a factory for $http which is used by $state we can't inject it directly
                // (this way we will get circular dependency error).
                // Using $injector.get will lead to having two instances of $http in our app.
                // By calling $injector.invoke we can delay injection to the moment when application is up & running,
                // therefore we will be injecting existing (and properly configured) $http instance.
                $injector.invoke(function ($state) {
                    $state.go(stateName);
                });
            }

            function success(response) {
                return response;
            }

            function error(response) {
                if (response.status === 401) { // user is not logged in
                    var UserSessionService = $injector.get('UserSessionService'); // uses $injector to avoid circular dependency
                    if (UserSessionService.isLogged()) {
                        UserSessionService.logout(); // Http session expired / logged out - logout on Angular layer
                        FlashService.set('Your session timed out. Please login again.');
                        redirectToState('login');
                    }
                } else if (response.status === 403) {
                    console.log(response.data);
                    // do nothing, user is trying to modify data without privileges
                } else if (response.status === 404) {
                    redirectToState('error404');
                } else if (response.status === 500) {
                    redirectToState('error500');
                } else {
                    redirectToState('error');
                }
                return $q.reject(response);
            }

            return {
                response : success,
                responseError : error
            };

        }];
        $httpProvider.interceptors.push(interceptor);
    }])
    .run(function ($rootScope, UserSessionService, $state) {

        function requireAuth(targetState) {
            return targetState && targetState.data && targetState.data.auth;
        }

        $rootScope.$on('$stateChangeStart', function(ev, targetState) {
            if(requireAuth(targetState) && UserSessionService.isNotLogged()) {
                $state.go('login', { page: targetState.url });
                ev.preventDefault();
            }
        });
    })
    .run(function ($rootScope, $timeout, FlashService, NotificationsService) {
        $rootScope.$on("$stateChangeSuccess", function () {
            var message = FlashService.get();
            NotificationsService.showInfo(message);
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
                var nonBlocking = false;
                if (typeof responseConfig === "object" && typeof responseConfig.headers === "object") {
                    nonBlocking = responseConfig.headers.dontBlockPageOnAjax;
                }
                return !nonBlocking;
            };
            success = function (response) {
                stopAjax(response.config);
                return response;
            };
            error = function (response) {
                stopAjax(response.config);
                return $q.reject(response);
            };
            return {
                response : success,
                responseError : error
            };
        }];
        return $httpProvider.interceptors.push(stopAjaxInterceptor);
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
