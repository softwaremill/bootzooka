"use strict";

angular.module('smlBootzooka.maintenance').controller('UptimeCtrl', function UptimeCtrl($scope, $timeout, UtilService) {

    $scope.update = function () {
        UtilService.loadUptime(function (data) {
            $scope.uptime = data.value;
        });
    };

    var uptimeEventId = $timeout(function updateUptimeLoop() {
        $scope.update();
        uptimeEventId = $timeout(updateUptimeLoop, 10000);
    }, 10000);

    $scope.$on("$routeChangeStart", function () {
        $timeout.cancel(uptimeEventId);
    });

    $scope.update();

});