export default ngModule => {
  ngModule.directive('fixedFooter', () => {
    require('./footer.css');
    return {
      restrict: 'E',
      scope: {},
      template: require('./footer.html')
    }
  })
};
