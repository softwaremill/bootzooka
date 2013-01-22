"use strict";

var directives = angular.module("smlBootstrap.directives", []);

directives.directive("bsBlur", function() {
    return function(scope, element, attrs) {
        element.bind("blur", function() {
            scope.$eval(attrs.bsBlur);
        });
    }
});
