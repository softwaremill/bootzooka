"use strict";

angular.module('smlBootzooka.common.directives').directive('focusOn', function () {
    return function (scope, elem, attr) {
        elem[0].focus();
    };
});