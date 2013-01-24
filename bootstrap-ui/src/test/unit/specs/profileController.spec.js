"user strict";

describe("Profile Controller", function () {
    beforeEach(module("smlBootstrap.services", "smlBootstrap.controllers"));
    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var $httpBackend, ctrl, scope, userSessionService, profileService;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_, UserSessionService, ProfileService) {
        UserSessionService.loggedUser = {
            login: "user1",
            email: "user1@sml.com"
        };
        userSessionService = UserSessionService;
        scope = $rootScope.$new();
        ctrl = $controller("ProfileController", {$scope: scope});
        ctrl.origEmail = "userold@sml.com";
        $httpBackend = _$httpBackend_;
    }));

    it("should get logged user", function () {
        expect(scope.user).toBe(userSessionService.loggedUser);
    });
    describe("when changing email", function () {
        it("should call ReST service when email changed", function () {
            scope.profileForm = {
               email: {
                   $dirty: true,
                   $pristine: false
               }
            };
            $httpBackend.expectPATCH("rest/users").respond("nothing");
            scope.changeEmail();
            $httpBackend.flush();
        });
        it("should not call ReST service when email didn't change", function() {
            scope.profileForm = {
                email: {
                    $dirty: false,
                    $pristine: true
                }
            };
            scope.changeEmail();
        });
        it("should update local data after successful change", function () {
            scope.profileForm = {
                email: {
                    $dirty: true,
                    $pristine: false
                }
            };
            $httpBackend.expectPATCH("rest/users").respond("nothing");
            scope.changeEmail();
            $httpBackend.flush();
            expect(ctrl.origEmail).toBe("user1@sml.com");
            expect(userSessionService.loggedUser.email).toBe("user1@sml.com");
        });
        it("should mark form as pristine after successful change", function () {
            scope.profileForm = {
                email: {
                    $dirty: true,
                    $pristine: false
                }
            };
            $httpBackend.expectPATCH("rest/users").respond("nothing");
            scope.changeEmail();
            $httpBackend.flush();
            expect(scope.profileForm.email.$dirty).toBe(false);
            expect(scope.profileForm.email.$pristine).toBe(true);
        });
    });

});