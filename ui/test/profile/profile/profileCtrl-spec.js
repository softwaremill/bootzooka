"use strict";

describe("Profile Controller", function () {
    beforeEach(module('smlBootzooka.profile'));
    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var $httpBackend, ctrl, scope, userSessionService;

    beforeEach(inject(function ($rootScope, $controller, _$httpBackend_, UserSessionService) {
        var user = {
            login: "user1",
            email: "user1@sml.com"
        };
        UserSessionService.loggedUser.init(user);
        userSessionService = UserSessionService;
        scope = $rootScope.$new();
        ctrl = $controller("ProfileCtrl", {$scope: scope});
        $httpBackend = _$httpBackend_;
        $httpBackend.expectGET('rest/users').respond(user);
        $httpBackend.flush();
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

    describe("when changing password", function () {
        beforeEach(function () {
            scope.passwordChangeForm = {
                currentPassword: {
                    $dirty: false
                },
                newPassword: {
                    $dirty: false
                },
                newPasswordRepeated: {
                    $dirty: false,
                    $error: {
                        repeat: false
                    }
                },
                $valid: true,
                $setPristine: function () {
                }
            };
        });
        it("should call ReST service when form is valid", function () {
            $httpBackend.expectPOST('rest/users/changepassword', '{"currentPassword":"pass","newPassword":"NewPass"}').respond('nothing');
            scope.currentPassword = 'pass';
            scope.newPassword = 'NewPass';
            scope.newPasswordRepeated = 'NewPass';
            scope.changePassword();
            $httpBackend.flush();
        });
        it("should not call ReST service when current password is missing", function () {
            scope.passwordChangeForm.$valid = false;
            scope.currentPassword = '';
            scope.changePassword();
        });
        it("should not call ReST service when new password is missing", function () {
            scope.passwordChangeForm.$valid = false;
            scope.currentPassword = 'pass';
            scope.newPassword = '';
            scope.changePassword();
        });
        it("should not call ReST service when repeated password is missing", function () {
            scope.passwordChangeForm.$valid = false;
            scope.currentPassword = 'pass';
            scope.newPassword = 'newPass';
            scope.newPasswordRepeated = '';
            scope.changePassword();
        });
        it("should not call ReST service when repeated password doesn't match new password", function () {
            scope.passwordChangeForm.$valid = false;
            scope.currentPassword = 'pass';
            scope.newPassword = 'newPass';
            scope.newPasswordRepeated = 'newwPass';
            scope.changePassword();
        });
        it("should mark form pristine after successful change", function () {
            $httpBackend.expectPOST('rest/users/changepassword', '{"currentPassword":"pass","newPassword":"NewPass"}').respond('nothing');
            scope.currentPassword = 'pass';
            scope.newPassword = 'NewPass';
            scope.newPasswordRepeated = 'NewPass';
            scope.changePassword();
            $httpBackend.flush();
            expect(scope.passwordChangeForm.$setPristine());
            expect(scope.currentPassword).toBe(undefined);
            expect(scope.newPassword).toBe(undefined);
            expect(scope.newPasswordRepeated).toBe(undefined);
        });
    });

});