'use strict';

describe('User Session Controller', function () {
    beforeEach(module('smlBootzooka.session'));

    describe('without logged user', function () {
        var scope, ctrl, userSessionService;

        beforeEach(inject(function ($rootScope, $controller, UserSessionService) {
            scope = $rootScope.$new();
            ctrl = $controller('UserSessionCtrl', {$scope: scope, FlashService: {}});
            userSessionService = UserSessionService;
        }));

        it('Should have user not logged', function () {
            expect(scope.isLogged()).toBe(false);
        });
    });

    describe('with user logged in', function () {
        var scope, ctrl, userSessionService, $httpBackend, $cookies;

        beforeEach(inject(function ($rootScope, $controller, _$cookies_, _$httpBackend_, $location, UserSessionService) {
            scope = $rootScope.$new();
            ctrl = $controller('UserSessionCtrl', {$scope: scope, FlashService: {}});
            userSessionService = UserSessionService;
            $cookies = _$cookies_;
            $cookies["scentry.auth.default.user"] = "Jan Kowalski";
            $httpBackend = _$httpBackend_;
            var user = {
                login: "User1",
                email: "test@test.pl"
            };
            $httpBackend.expectGET('rest/users').respond(user);
            $httpBackend.flush();
        }));

        it("should know user is logged in", function () {
            expect(scope.isLogged()).toBe(true);
        });

        it("user should be able to log out", function () {
            // Given
            $httpBackend.expectGET('rest/users/logout').respond(['anything']); // resource#query returns array
            expect(userSessionService.loggedUser).not.toBe(null);

            // When
            scope.logout();
            $httpBackend.flush();

            // Then
            expect(scope.isLogged()).toBe(false);
            expect(userSessionService.loggedUser.isLogged).toBe(false);
        });
    });
});