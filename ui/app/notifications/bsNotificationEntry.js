'use strict';

angular.module('smlBootzooka.notifications')
    .directive('bsNotificationEntry', function ($timeout) {

        function setUpElementAttributes(element, typeOrUndefined) {
            var type = typeOrUndefined || 'info';
            element.addClass('alert');
            element.addClass('alert-dismissible');
            element.addClass('alert-' + type);
            element.attr('role', 'alert');
        }

        return {
            restrict: 'A',
            templateUrl: 'notifications/notificationEntry.html',
            link: function (scope, element) {
                var alertFadeOutPromise = $timeout(function () {
                    element.fadeOut(2000, function () {
                        element.remove();
                    });
                }, 3000);

                setUpElementAttributes(element, scope.message.type);

                element.fadeOut(0, function () {
                    element.fadeIn(300);
                });

                // enforce scope destroy after message is closed by the user.
                element.on('$destroy', function () {
                    scope.$destroy();
                });

                scope.$on('$destroy', function () {
                    $timeout.cancel(alertFadeOutPromise);
                    scope.notificationsSource.dismiss(scope.message);
                });
            }
        };
    });
