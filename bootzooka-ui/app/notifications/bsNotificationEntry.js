"use strict";

angular.module('smlBootzooka.directives')
    .directive('bsNotificationEntry', function ($timeout) {

        function setUpElementAttributes(element, typeOrUndefined) {
            var type = typeOrUndefined || 'info';
            element.addClass('alert');
            element.addClass('alert-dismissible');
            element.addClass('alert-' + type);
            element.attr('role', 'alert');
        }

        return {
            require: '^bsNotificationsContainer',
            restrict: 'A',
            templateUrl: 'notifications/notificationEntry.html',
            link: function (scope, element, attributes, bsNotificationsContainerCtrl) {
                var alertFadeOutPromise = $timeout(function () {
                    element.fadeOut(2000, function () {
                        element.remove();
                    });
                }, 3000);

                setUpElementAttributes(element, scope.message.type);

                element.fadeOut(0, function () {
                    element.fadeIn(300);
                });

                element.on('$destroy', function () {
                    $timeout.cancel(alertFadeOutPromise);
                    bsNotificationsContainerCtrl.remove(scope.message);
                });
            }
        };
    });
