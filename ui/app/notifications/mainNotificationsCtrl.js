'use strict';

angular.module('smlBootzooka.notifications')
    .controller('MainNotificationsCtrl', function MainNotificationsCtrl($scope, NotificationsService) {

        $scope.notificationsService = NotificationsService;

    });
