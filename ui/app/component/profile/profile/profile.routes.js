profileRoutes.$inject = ['$stateProvider'];

export default function profileRoutes($stateProvider) {
  $stateProvider.state('profile', {
    url: '/profile',
    controller: 'ProfileCtrl',
    template: require('./profile.html'),
    resolve: {
      //this is a kind of constructor injection to controller, since ProfileCtrl require logged user.
      user: UserSessionService => UserSessionService.getLoggedUserPromise()
    },
    data: {
      auth: true
    }
  });
}
