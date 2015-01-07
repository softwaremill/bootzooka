"use strict";

angular.module('smlBootzooka.directives').directive('bsRepeatPassword', function() {
    return {
        require: "ngModel",
        link: function (scope, elem, attrs, ctrl) {
            var otherInput = elem.inheritedData("$formController")[attrs.bsRepeatPassword];

            function requireToHaveSameValueAs(input) {
                return function (value) {
                    ctrl.$setValidity("repeat", value === input.$viewValue);
                    return value;
                };
            }

            ctrl.$parsers.push(requireToHaveSameValueAs(otherInput));
            otherInput.$parsers.push(requireToHaveSameValueAs(ctrl));
        }
    };
});
