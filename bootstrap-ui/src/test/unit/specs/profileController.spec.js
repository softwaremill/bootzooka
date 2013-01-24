"user strict";

describe("Profile Controller", function () {
    beforeEach(module("smlBootstrap.services", "smlBootstrap.controllers"));
    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var $httpBackend, ctrl, scope, userSessionService;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_, UserSessionService) {
        UserSessionService.loggedUser = {
            login: "user1",
            email: "user1@sml.com"
        };
        userSessionService = UserSessionService;
        scope = $rootScope.$new();
        ctrl = $controller("ProfileController", {$scope: scope});
        $httpBackend = _$httpBackend_;
    }));

    var withValidForm = function () {
        scope.profileForm = {
            email: {
                $dirty: true,
                $pristine: false,
                $valid: true
            },
            login: {
                $dirty: true,
                $pristine: false,
                $valid: true,
                $error: {}
            }
        };
    };

    it("should get logged user", function () {
        scope.login = "user1";
        scope.email = "user1@sml.com";
        expect(scope.login).toBe(userSessionService.loggedUser.login);
        expect(scope.email).toBe(userSessionService.loggedUser.email);
    });

    describe("when changing login", function () {
        var performLoginChange = function (login) {
            $httpBackend.expectPATCH('rest/users', '{"login":"' + login + '"}').respond("nothing");
            scope.login = login;
            scope.changeLogin();
            $httpBackend.flush();
        };
        it("should call ReST service when login changed", function () {
            withValidForm();
            performLoginChange("newlogin");
        });
        it("should not call ReST service when login didn't change", function () {
            scope.profileForm = {
                login: {
                    $dirty: false,
                    $pristine: true,
                    $valid: true,
                    $error: {}
                }
            };
            scope.changeLogin();
        });
        it("should not call ReST service when login is invalid", function () {
            scope.profileForm = {
                login: {
                    $dirty: true,
                    $pristine: false,
                    $valid: false,
                    $error: {}
                }
            };
            scope.changeLogin();
        });
        it("should update local data after successful change", function () {
            withValidForm();
            performLoginChange("newlogin");
            expect(userSessionService.loggedUser.login).toBe("newlogin");
        });
        it("should make form pristine after successful change", function () {
            withValidForm();
            performLoginChange("newlogin");
            expect(scope.profileForm.login.$pristine).toBe(true);
            expect(scope.profileForm.login.$dirty).toBe(false);
        });
    });

    describe("when changing email", function () {

        it("should call ReST service when email changed", function () {
            withValidForm();
            $httpBackend.expectPATCH('rest/users', '{"email":"newMail@sml.com"}').respond("nothing");
            scope.email = "newMail@sml.com";
            scope.changeEmail();
            $httpBackend.flush();
        });
        it("should not call ReST service when email didn't change", function () {
            scope.profileForm = {
                email: {
                    $dirty: false,
                    $pristine: true,
                    $valid: true
                }
            };
            scope.changeEmail();
        });
        it("should not call ReST service if email is invalid", function () {
            scope.profileForm = {
                email: {
                    $dirty: true,
                    $pristine: false,
                    $valid: false
                }
            };
            scope.changeEmail();
        });
        it("should update local data after successful change", function () {
            withValidForm();
            $httpBackend.expectPATCH('rest/users').respond("nothing");
            scope.email = "newMail@sml.com";
            scope.changeEmail();
            $httpBackend.flush();
            expect(userSessionService.loggedUser.email).toBe(scope.email);
        });
        it("should mark form as pristine after successful change", function () {
            withValidForm();
            $httpBackend.expectPATCH('rest/users').respond("nothing");
            scope.email = "newMail@sml.com";
            scope.changeEmail();
            $httpBackend.flush();
            expect(scope.profileForm.email.$dirty).toBe(false);
            expect(scope.profileForm.email.$pristine).toBe(true);
        });
    });

});