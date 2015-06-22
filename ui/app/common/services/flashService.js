'use strict';

angular.module('smlBootzooka.common.services').factory('FlashService', function () {

    var queue = [];

    return {
        set: function (message) {
            queue.push(message);
        },
        get: function () {
            return queue.shift();
        }
    };
});
