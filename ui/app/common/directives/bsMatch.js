/**
 * Link 2 form fields and force matching
 */
angular.module('smlBootzooka.common.directives').directive('bsMatch', function () {
    return {
        require: 'ngModel',
        restrict: 'A',
        link: function(scope, elem, attrs, ctrl) {
            scope.$watch(function() {
                return (ctrl.$pristine && angular.isUndefined(ctrl.$modelValue)) || scope.$eval(attrs.bsMatch) === ctrl.$modelValue;
            }, function(currentValue) {
                ctrl.$setValidity('match', currentValue);
            });
        }
    };
});
