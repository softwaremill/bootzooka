'use strict';

describe('Login Controller', function () {

    beforeEach(module('smlBootzooka.profile'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, state;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
        $httpBackend = _$httpBackend_;
        scope = $rootScope.$new();
        state = {
            current: null,
            go: function (newState) {
                this.current = newState;
            }
        };
        ctrl = $controller('LoginCtrl', {
            $scope: scope,
            $state: state
        });
        $httpBackend.expectGET('api/users').respond(401);
        $httpBackend.flush();
        scope.loginForm = {
            login: {
                $dirty: false
            },
            password: {
                $dirty: false
            },
            $invalid: false
        };
    }));

    it('Should call login rest service when form is valid', function () {
        // Given
        $httpBackend.expectPOST('api/users').respond('anything');

        // When
        scope.login();
        $httpBackend.flush();
        
        //then
        expect(state.current).toBe('main');
    });

    it('Should not call login rest service when form is invalid', function () {
        // Given
        scope.loginForm.$invalid = true;

        // When
        scope.login();

        // Then
        // verifyNoOutstandingRequest(); is checked after each test
    });

});
