export default ngModule => {
  ngModule.directive('fixedHeader', () => {
    require('./header.css');
    return {
      restrict: 'E',
      scope: {},
      template: require('./header.html')
    }
  })
};