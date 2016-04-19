intercept.$inject = ['$httpProvider'];
export function intercept($httpProvider) {
  let interceptor = ['$rootScope', '$q', '$injector', '$log', 'NotificationsService', function ($rootScope, $q, $injector, $log, NotificationsService) {
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
        $log.warn(response.data);
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
}

stateChangeStart.$inject = ['$rootScope', 'UserSessionService', 'FlashService', '$state'];
export function stateChangeStart($rootScope, UserSessionService, FlashService, $state) {

  function requireAuth(targetState) {
    return targetState && targetState.data && targetState.data.auth;
  }

  $rootScope.$on('$stateChangeStart', (ev, targetState, targetParams) => {
    if (requireAuth(targetState) && UserSessionService.isNotLogged()) {
      ev.preventDefault();
      UserSessionService.getLoggedUserPromise().then(() => {
        $state.go(targetState, targetParams);
      }, () => {
        UserSessionService.saveTarget(targetState, targetParams);
        $state.go('login');
      });
    }
  });

  $rootScope.$on('401', () => {
    if (UserSessionService.isLogged()) {
      UserSessionService.resetLoggedUser();
      FlashService.set('Your session timed out. Please login again.');
    }
  });
}

stateChangeSuccess.$inject = ['$rootScope', 'FlashService', 'NotificationsService'];
export function stateChangeSuccess($rootScope, FlashService, NotificationsService) {
  $rootScope.$on('$stateChangeSuccess', () => {
    let message = FlashService.get();
    NotificationsService.showInfo(message);
  });
}

routing.$inject = ['$urlRouterProvider', '$stateProvider'];
export function routing($urlRouterProvider, $stateProvider) {
  $urlRouterProvider.when('', '/');
  $urlRouterProvider.otherwise('/error404');

  $stateProvider
    .state('error404', {
      url: '/error404',
      template: require('./common/errorpages/error404.html')
    });
}
