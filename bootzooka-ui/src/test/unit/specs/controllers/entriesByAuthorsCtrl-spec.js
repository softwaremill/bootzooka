'use strict';

describe('Entries by authors controller', function () {

    var allUsers = angular.toJson([
        {id: '1', login: 'user1', email: 'a@b.pl'},
        {id: '2', login: 'user2', email: 'j@k.pl'}
    ]);

    var entries = angular.toJson([
        {id: '1', text: 'entry 1', author: 'user1'},
        {id: '2', text: 'entry 2', author: 'user1'}
    ]);

    beforeEach(module('smlBootzooka.entries'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    describe('with no author specified', function () {
        var $httpBackend, ctrl, scope, location;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $controller, $location) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/users/all').respond(allUsers);

            location = $location;

            scope = $rootScope.$new();
            ctrl = $controller('EntriesByAuthorsCtrl', {$scope: scope});

            $httpBackend.flush();
        }));

        it('should have no selected author', function () {
            expect(scope.authorSelected()).toBe(false);
        });

        it('should have no entries', function () {
            expect(scope.noEntries()).toBe(true);
        });

        it('should have two authors', function () {
            expect(scope.authors.length).toBe(2);
        });

        it('should navigate to author\'s entries after selecting an author', function () {
            // when
            scope.showEntriesAuthoredBy({id: '3'});

            // then
            expect(location.path()).toBe('/entries/author/3');
        });
    });

    describe('with author specified', function () {
        var $httpBackend, ctrl, scope, userSessionService;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/users/all').respond(allUsers);
            $httpBackend.whenGET('/rest/entries/author/1').respond(entries);

            $routeParams.authorId = 1;

            scope = $rootScope.$new();
            ctrl = $controller('EntriesByAuthorsCtrl', {$scope: scope});

            $httpBackend.flush();
        }));

        it('should have selected author', function () {
            expect(scope.authorSelected()).toBe(true);
        });

        it('should have two entries', function () {
            expect(scope.entries.length).toBe(2);
        });

        it('should have two authors', function () {
            expect(scope.authors.length).toBe(2);
        });

        it('should mark author with id=1 as current', function () {
            expect(scope.isCurrentAuthor({id: 1})).toBe(true);
        });

        it('should not mark author with id=2 as current', function () {
            expect(scope.isCurrentAuthor({id: 2})).toBe(false);
        });

        describe('and no logged user', function () {
            it('should never return true for isOwner', function () {
                angular.forEach(scope.entries, function (entry) {
                    expect(scope.isOwnerOf(entry)).toBe(false);
                });
            });
        });

        describe('and author same as logged user', function () {
            beforeEach(inject(function ($cookies, UserSessionService) {
                userSessionService = UserSessionService;
                userSessionService.loggedUser = {
                    login: "user1"
                };
                $cookies["scentry.auth.default.user"] = "user1";
            }));

            it('should mark logged user as owner of all entries', function () {
                angular.forEach(scope.entries, function (entry) {
                    expect(scope.isOwnerOf(entry)).toBe(true);
                });
            });
        });

        describe('and author other than logged user', function () {
            beforeEach(inject(function ($cookies, UserSessionService) {
                userSessionService = UserSessionService;
                userSessionService.loggedUser = {
                    login: "user2"
                };
                $cookies["scentry.auth.default.user"] = "user2";
            }));

            it('should not mark logged user as owner of any entry', function () {
                angular.forEach(scope.entries, function (entry) {
                    expect(scope.isOwnerOf(entry)).toBe(false);
                });
            });
        });
    });
});
