'use strict';

describe('Version Controller', function () {
    beforeEach(module('smlBootzooka.version'));

    var scope, $httpBackend;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_) {
        scope = $rootScope.$new();
        $httpBackend = _$httpBackend_;
        $controller('VersionCtrl', {$scope: scope});
    }));

    it('should retrieve application version', function () {
        // given
        $httpBackend.expectGET('api/version').respond({build: 'foo', date: 'bar'});

        // when
        $httpBackend.flush();

        // then
        expect(scope.buildSha).toEqual('foo');
        expect(scope.buildDate).toEqual('bar');
    });
});