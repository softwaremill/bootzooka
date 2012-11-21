'use strict';

describe("Login Controller", function () {

    beforeEach(module('userSessionService'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        ctrl = $controller(LoginController, {$scope: scope});

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
        $httpBackend.expectPOST('rest/users').respond('anything');

        // When
        scope.login();
        $httpBackend.flush();
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
