passwordRoutes.$inject = ['$stateProvider'];
//above line must remain, because of error only on npm run dist
//http://errors.angularjs.org/1.5.3/$injector/modulerr?p0=app&p1=Error%3A%20%…0%20%20%20at%20oe%20(http%3A%2F%2F127.0.0.1%3A9090%2Fbundle.js%3A7%3A21330

export default function passwordRoutes($stateProvider) {
  $stateProvider.state('recover-lost-password', {
      url: '/recover-lost-password',
      controller: 'PasswordResetCtrl',
      template: require('./recover-lost-password.html')
    })
    .state('password-reset', {
      url: '/password-reset?code',
      controller: 'PasswordResetCtrl',
      template: require('./password-reset.html')
    });
}
