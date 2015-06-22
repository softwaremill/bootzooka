'use strict';

describe('Flash Service', function () {

    beforeEach(module('smlBootzooka.common.services'));

    var scope, srv;

    beforeEach(inject(function ($rootScope, $injector) {

        scope = $rootScope.$new();
        srv = $injector.get('FlashService');

    }));

    it('Should push message to service', function () {
        // Given
        srv.set('My Message');

        // When
        var message = srv.get();

        // Then
        expect(message).toBe('My Message');
    });

    it('Should retrieve message on event', function () {
        // Given
        srv.set('My Second Message');

        var message = '';
        scope.$on('MyEvent', function () {
            message = srv.get();
        });
        // When
        scope.$emit('MyEvent');

        // Then
        expect(message).toBe('My Second Message');
    });

});
