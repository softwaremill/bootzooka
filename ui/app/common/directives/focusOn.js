'use strict';

angular.module('smlBootzooka.common.directives').directive('focusOn', () =>
  (scope, elem) =>  elem[0].focus()
);
