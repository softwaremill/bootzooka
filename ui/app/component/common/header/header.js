export default ngModule => {
  ngModule.directive('fixedHeader', ($log) => {
    require('./header.css');
    return {
      restrict: 'E',
      scope: {},
      template: require('./header.html')
    }
  })
};
