"use strict";

var directives = angular.module("smlBootstrap.directives", []);

directives.directive("bsBlur", function() {
    return function(scope, element, attrs) {
        element.bind("blur", function() {
            scope.$eval(attrs.bsBlur);
        });
    }
});

directives.directive("stringMatches", function() {
    return {
        require: "ngModel",
        link: function(scope, elem, attrs, ctrl) {
            ctrl.$parsers.unshift(function(viewValue) {
                if(viewValue === scope[attrs.stringMatches]) {
                    ctrl.$setValidity("match", true);
                    return viewValue;
                } else {
                    ctrl.$setValidity("match", false);
                    return undefined;
                }
            });
        }
    };
});
