'use strict';

describe('Flash Service', function () {

    beforeEach(module('smlBootzooka.notifications'));

    var scope, NotificationsService;

    beforeEach(inject(function ($rootScope, $injector) {
        scope = $rootScope.$new();
        NotificationsService = $injector.get('NotificationsService');
    }));

    it('Should not contain any messages by default', function () {
        // Then
        expect(NotificationsService.messages).toEqual([]);
    });

    it('Should not push message when neither title AND content are defined', function () {
        // When
        NotificationsService.show('some type')();

        // Then
        expect(NotificationsService.messages).toEqual([]);
    });

    it('Should push message', function () {
        // Given
        var msg = {
            type: 'some type',
            title: 'some header',
            content: 'some content'
        };

        // When
        NotificationsService.show(msg.type)(msg.content, msg.title);

        // Then
        expect(NotificationsService.messages[0]).toEqual(msg);
    });

    it('Should aggregate many messages', function () {
        // Given
        var firstMsg = {
                type: 'some type',
                title: 'first header',
                content: 'first content'
            },
            secondMsg = {
                type: 'some type',
                title: 'second header',
                content: 'second content'
            };

        // When
        NotificationsService.show(firstMsg.type)(firstMsg.content, firstMsg.title);
        NotificationsService.show(secondMsg.type)(secondMsg.content, secondMsg.title);

        // Then
        expect(NotificationsService.messages).toEqual([firstMsg, secondMsg]);
    });

    it('Should push info message', function () {
        // Given
        var msg = {
            type: 'info',
            title: 'info header',
            content: 'info content'
        };

        // When
        NotificationsService.showInfo(msg.content, msg.title);

        // Then
        expect(NotificationsService.messages[0]).toEqual(msg);
    });

    it('Should push error message', function () {
        // Given
        var msg = {
            type: 'danger',
            title: 'error header',
            content: 'error content'
        };

        // When
        NotificationsService.showError(msg.content, msg.title);

        // Then
        expect(NotificationsService.messages[0]).toEqual(msg);
    });

    it('Should push success message', function () {
        // Given
        var msg = {
            type: 'success',
            title: 'success header',
            content: 'success content'
        };

        // When
        NotificationsService.showSuccess(msg.content, msg.title);

        // Then
        expect(NotificationsService.messages[0]).toEqual(msg);
    });

    it('Should push warning message', function () {
        // Given
        var msg = {
            type: 'warning',
            title: 'error header',
            content: 'error content'
        };

        // When
        NotificationsService.showWarning(msg.content, msg.title);

        // Then
        expect(NotificationsService.messages[0]).toEqual(msg);
    });

});
