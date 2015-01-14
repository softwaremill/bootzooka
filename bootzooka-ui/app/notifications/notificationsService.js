"use strict";

angular.module('smlBootzooka.notifications')
    .service('NotificationsService', function () {
        var self = this;

        function checkPreconditions(title, content) {
            return angular.isDefined(content) || angular.isDefined(title);
        }

        function show(type) {
            return function (content, title) {
                if (!checkPreconditions(content, title)) {
                    return;
                }
                self.messages.push({
                    type: type || 'info',
                    title: title,
                    content: content
                });
            };
        }

        this.messages = [];

        this.showInfo = show('info');

        this.showError = show('danger');

        this.showSuccess = show('success');

        this.showWarning = show('warning');

        this.show = show;

        this.dismiss = function (message) {
            var index = this.messages.indexOf(message);
            if (index >= 0) {
                this.messages.splice(index, 1);
            }
        };
    });