"use strict";

angular.module('smlBootstrap.filters').filter('newlines', function () {
    return function (text) {
        return text.replace(/\n/g, '<br/>');
    };
});
