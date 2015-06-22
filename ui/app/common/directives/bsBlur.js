'use strict';

angular.module('smlBootzooka.common.directives').directive('bsBlur', function () {
    return function (scope, element, attrs) {
        element.bind('blur', function () {
            scope.$eval(attrs.bsBlur);
        });
    };
});