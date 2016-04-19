'use strict';
export default ngModule => {
    ngModule.directive('bsBlur', () =>
        (scope, element, attrs) => element.bind('blur', () => scope.$eval(attrs.bsBlur))
    );     
}