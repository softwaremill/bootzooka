'use strict';

describe("Uptime Controller", function () {

    beforeEach(module('smlBootstrap.services', 'smlBootstrap.controllers'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
        $httpBackend = _$httpBackend_;
        $httpBackend.whenGET('/rest/uptime').respond('{"value":100}');

        scope = $rootScope.$new();
        scope.uptime = 0;
        ctrl = $controller('UptimeController', {$scope: scope});

        $httpBackend.flush();
    }));

    it('should update uptime value', function () {
        // given
        $httpBackend.expectGET('/rest/uptime').respond('{"value":100}');

        // when
        scope.update();
        $httpBackend.flush();

        // then
        expect(scope.uptime).toBe(100);
    });

    it("should update uptime value on $timeout", inject(function ($timeout) {
        // given
        $httpBackend.expectGET('/rest/uptime').respond('{"value":120}');

        // when
        $timeout.flush();
        $httpBackend.flush();

        // then
        expect(scope.uptime).toBe(120);
    }));

});
