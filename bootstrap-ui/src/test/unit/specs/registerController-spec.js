'use strict';

describe("Register Controller", function () {

    beforeEach(module('registerService', 'smlBootstrap.services', 'smlBootstrap.controllers'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        ctrl = $controller('RegisterController', {$scope: scope});

        scope.user = {
            password: '',
            repeatPassword: ''
        };

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
                $dirty: false,
                $error: {
                    dontMatch: false
                }
            },
            $invalid: false,
            $valid: true
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
        scope.registerForm.$valid = false;

        // When
        scope.register();

        // Then
        // verifyNoOutstandingRequest(); is checked after each test
    });

    it('Should set error flag when passwords don\'t match', function () {
        // Given
        expect(scope.registerForm.repeatPassword.$error.dontMatch).toBe(false);

        scope.user.password = "secret";
        scope.user.repeatPassword = "othersecret";

        // When
        scope.checkPassword();

        // Then
        expect(scope.registerForm.repeatPassword.$error.dontMatch).toBe(true);
    });

    it('Should not set error flag when passwords match', function () {
        // Given
        expect(scope.registerForm.repeatPassword.$error.dontMatch).toBe(false);

        scope.user.password = "secret";
        scope.user.repeatPassword = "secret";

        // When
        scope.checkPassword();

        // Then
        expect(scope.registerForm.repeatPassword.$error.dontMatch).toBe(false);
    });

});
