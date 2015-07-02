'use strict';

describe('User Session Controller', function () {

    beforeEach(module('smlBootzooka.session'));
    beforeEach(module('smlBootzooka.common.services'));

    var userSessionService;

    var testWindow;
    
    beforeEach(function() {
        testWindow = {location: "dummy" };

        module(function($provide) {
            $provide.value('$window', testWindow);
        });
        inject(function($injector) {
            userSessionService = $injector.get('UserSessionService');
        });
    });

    describe('without logged user', function () {
        var scope, ctrl;

        beforeEach(inject(function ($rootScope, $controller, FlashService) {
            scope = $rootScope.$new();
            ctrl = $controller('UserSessionCtrl', {$scope: scope, FlashService: FlashService});
        }));

        it('Should have user not logged', function () {
            expect(scope.isLogged()).toBe(false);
        });
    });

    describe('with user logged in', function () {
        var scope, ctrl, $httpBackend, $cookies;

        beforeEach(inject(function ($rootScope, $controller, _$cookies_, _$httpBackend_, $location, FlashService) {
            scope = $rootScope.$new();
            ctrl = $controller('UserSessionCtrl', {$scope: scope, FlashService: FlashService});
            $cookies = _$cookies_;
            $cookies['scentry.auth.default.user'] = 'Jan Kowalski';
            $httpBackend = _$httpBackend_;
            var user = {
                login: 'User1',
                email: 'test@test.pl'
            };
            $httpBackend.expectGET('api/users').respond(user);
            $httpBackend.flush();
        }));

        it('should know user is logged in', function () {
            expect(scope.isLogged()).toBe(true);
        });

        it('user should be able to log out', function () {
            // Given
            $httpBackend.expectGET('api/users/logout').respond(['anything']); // resource#query returns array
            expect(userSessionService.getLoggedUser()).not.toBe(null);

            // When
            scope.logout();
            $httpBackend.flush();

            // Then
            expect(scope.isLogged()).toBe(false);
            expect(userSessionService.isLogged()).toBe(false);
            expect(testWindow.location).toBe("/");
        });
    });
});