'use strict';

describe('Version Controller', () => {
  beforeEach(module('smlBootzooka.version'));

  let scope, $httpBackend;

  beforeEach(inject(($rootScope, $controller, _$httpBackend_) => {
    scope = $rootScope.$new();
    $httpBackend = _$httpBackend_;
    $controller('VersionCtrl', {$scope: scope});
  }));

  it('should retrieve application version', ()=> {
    // given
    $httpBackend.expectGET('api/version').respond({build: 'foo', date: 'bar'});

    // when
    $httpBackend.flush();

    // then
    expect(scope.buildSha).toEqual('foo');
    expect(scope.buildDate).toEqual('bar');
  });
});
