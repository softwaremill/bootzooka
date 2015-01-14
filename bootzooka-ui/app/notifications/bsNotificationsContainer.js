"use strict";

angular.module('smlBootzooka.directives')
    .directive('bsNotificationsContainer', function (NotificationsService) {

        return {
            restrict: 'A',
            controller: function ($scope) {
                this.remove = function (message) {
                    NotificationsService.dismiss(message);
                };
                $scope.messages = NotificationsService.messages;
            },
            template: '<div ng-repeat="message in messages" bs-notification-entry></div>'
        };
    });