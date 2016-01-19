/**
 * Link 2 form fields and force matching
 */
angular.module('smlBootzooka.common.directives').directive('bsMatch', () => {
  return {
    require: 'ngModel',
    restrict: 'A',
    link: (scope, elem, attrs, ctrl) => {
      scope.$watch(() => {
        return (ctrl.$pristine && angular.isUndefined(ctrl.$modelValue)) || scope.$eval(attrs.bsMatch) === ctrl.$modelValue;
      }, currentValue =>  ctrl.$setValidity('match', currentValue));
    }
  };
});
