'use strict';

angular.module('smlBootzooka.notifications')
    .directive('bsNotifications', function () {

        function isValidNotificationsSource(notificationsSource) {
            return angular.isDefined(notificationsSource) &&
                angular.isArray(notificationsSource.messages) &&
                angular.isFunction(notificationsSource.dismiss);
        }

        return {
            restrict: 'E',
            scope: {
                notificationsSource: '='
            },
            link: function (scope) {
                if (!isValidNotificationsSource(scope.notificationsSource)) {
                    throw 'Incompatible notifications source. Check if object passed in \'notifications-source\' ' +
                        'has method \'dismiss\' and contains field \'messages\', an array of messages to be displayed.';
                }
            },
            template: '<div ng-repeat="message in notificationsSource.messages" bs-notification-entry></div>'
        };
    });