'use strict';

describe('Register Controller', function () {

    beforeEach(module('smlBootzooka.profile'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $controller) {
        $httpBackend = _$httpBackend_;

        scope = $rootScope.$new();
        ctrl = $controller('RegisterCtrl', {$scope: scope});

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
                    repeat: false
                }
            },
            $invalid: false,
            $valid: true
        };
    }));

    it('Should call register rest service when form is valid', function () {
        // Given
        $httpBackend.expectPOST('api/users/register').respond('anything');

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

    it('should not call register rest service when passwords don\'t match', function () {
        //Given
        scope.registerForm.repeatPassword.$error.repeat = true;

        //When
        scope.register();

        //Then
        //no outstanding requests (checked after test)
    });

});
