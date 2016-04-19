privateRoutes.$inject = ['$stateProvider'];

export default function privateRoutes($stateProvider) {
  $stateProvider.state('main', {
      url: '/main',
      template: require('./private.html'),
      data: {
        auth: true
      }
    });
}
