'use strict';

angular.module('smlBootzooka.common.directives').directive('bsBlur', () =>
  (scope, element, attrs) => element.bind('blur', () => scope.$eval(attrs.bsBlur))
);
