'use strict';

angular.module('smlBootzooka.common.directives', []);
angular.module('smlBootzooka.common.filters', []);
angular.module('smlBootzooka.common.services', []);
angular.module('smlBootzooka.common', ['smlBootzooka.common.filters', 'smlBootzooka.common.directives', 'smlBootzooka.common.services']);
angular.module('smlBootzooka.notifications', []);
angular.module('smlBootzooka.version', []);
angular.module('smlBootzooka.session', ['ngCookies', 'ngResource']);
var smlBootzooka = angular.module('smlBootzooka', ['smlBootzooka.templates', 'smlBootzooka.profile', 'smlBootzooka.session', 'smlBootzooka.common', 'smlBootzooka.notifications', 'smlBootzooka.version', 'ngSanitize', 'ui.router']);
var profile = angular.module('smlBootzooka.profile', ['ui.router', 'smlBootzooka.session', 'smlBootzooka.common', 'smlBootzooka.notifications']);

profile.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.when('', '/');
    $stateProvider
        .state('login', {
            url: '/login',
            controller: 'LoginCtrl',
            templateUrl: 'profile/login/login.html',
            params: {
                // these are used to redirect to a secure page if the user is not logged in
                // see the $stateChangeStart handler below and LoginCtrl
                targetState: null,
                targetParams: null
            }
        })
        .state('register', {
            url: '/register',
            controller: 'RegisterCtrl',
            templateUrl: 'profile/register/register.html'
        })
        .state('recover-lost-password', {
            url: '/recover-lost-password',
            controller: 'PasswordRecoveryCtrl',
            templateUrl: 'profile/password/recover-lost-password.html'
        })
        .state('password-reset', {
            url: '/password-reset?code',
            controller: 'PasswordRecoveryCtrl',
            templateUrl: 'profile/password/password-reset.html'
        })
        .state('profile', {
            url: '/profile',
            controller: 'ProfileCtrl',
            templateUrl: 'profile/profile/profile.html',
            resolve: {
                //this is a kind of constructor injection to controller, since ProfileCtrl require logged user.
                user: function (UserSessionService) {
                    return UserSessionService.getLoggedUserPromise();
                }
            },
            data: {
                auth: true
            }
        });
});

smlBootzooka.config(function ($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/error404');

    $stateProvider
        .state('error404', {
            url: '/error404',
            templateUrl: 'common/errorpages/error404.html'
        })
        .state('main', {
            url: '/main',
            templateUrl: 'common/private.html',
            data: {
                auth: true
            }
        })
        .state('home', {
            url: '/',
            templateUrl: 'common/public.html'
        });
});

smlBootzooka.config(['$httpProvider', function ($httpProvider) {
    var interceptor = ['$rootScope', '$q', '$injector', '$log', 'NotificationsService', function ($rootScope, $q, $injector, $log, NotificationsService) {

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
                $rootScope.$emit('401');
            } else if (response.status === 403) {
                $log.warn(response.data);
                // do nothing, user is trying to modify data without privileges
            } else if (response.status === 404) {
                redirectToState('error404');
            } else if (response.status === 409) {
                NotificationsService.showError(response);
            } else {
                NotificationsService.showError('Something went wrong..', 'Unexpected error');
            }
            return $q.reject(response);
        }

        return {
            response: success,
            responseError: error
        };

    }];
    $httpProvider.interceptors.push(interceptor);
}]);

smlBootzooka.run(function ($rootScope, UserSessionService, FlashService, $state) {

    function requireAuth(targetState) {
        return targetState && targetState.data && targetState.data.auth;
    }

    $rootScope.$on('$stateChangeStart', function (ev, targetState, targetParams) {
        if (requireAuth(targetState) && UserSessionService.isNotLogged()) {
            ev.preventDefault();
            UserSessionService.getLoggedUserPromise().then(function () {
                $state.go(targetState, targetParams);
            }, function () {
                UserSessionService.saveTarget(targetState, targetParams);
                $state.go('login');
            });
        }
    });

    $rootScope.$on('401', function () {
        if (UserSessionService.isLogged()) {
            UserSessionService.resetLoggedUser();
            FlashService.set('Your session timed out. Please login again.');
        }
    });
});

smlBootzooka.run(function ($rootScope, $timeout, FlashService, NotificationsService) {
    $rootScope.$on('$stateChangeSuccess', function () {
        var message = FlashService.get();
        NotificationsService.showInfo(message);
    });
});

