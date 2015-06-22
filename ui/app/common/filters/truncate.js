angular.module('smlBootzooka.common.filters')

    .filter('truncate', function () {
        return function (text, length, end) {
            length = Number(length) || 10;
            end = end || '...';

            if (text.length <= length || text.length - end.length <= length) {
                return text;
            }
            else {
                return text.substring(0, length-end.length) + end;
            }

        };
    });
