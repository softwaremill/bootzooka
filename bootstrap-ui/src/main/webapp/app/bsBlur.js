"use strict";

angular.module('smlBootstrap.directives').directive('bsBlur', function() {
    return function(scope, element, attrs) {
        element.bind("blur", function() {
            scope.$eval(attrs.bsBlur);
        });
    }
});