'use strict';

describe("Register Controller", function () {

    beforeEach(module('registerService', 'smlBootstrap.controllers'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        ctrl = $controller('RegisterController', {$scope: scope});

        scope.registerForm = {
            login: {
                $dirty: false
            },
            email: {
                $dirty: false
            },
            password: {
                $dirty: false
            },
            repeatPassword: {
                $dirty: false
            },
            $invalid: false
        };
    }));

    it('Should call register rest service when form is valid', function () {
        // Given
        $httpBackend.expectPUT('rest/users/register').respond('anything');

        // When
        scope.register();
        $httpBackend.flush();
    });

    it('Should not call register rest service when form is invalid', function () {
        // Given
        scope.registerForm.$invalid = true;

        // When
        scope.register();

        // Then
        // verifyNoOutstandingRequest(); is checked after each test
    });

});
