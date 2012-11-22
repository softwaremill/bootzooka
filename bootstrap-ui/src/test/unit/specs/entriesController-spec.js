'use strict';

describe("Entries Controller", function () {

    beforeEach(module('entriesService', 'userSessionService'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    describe('with not-empty data response and logged user', function () {
        var scope, $httpBackend, ctrl;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/entries/count').respond('{"value":2}');
            $httpBackend.whenGET('rest/entries').respond('[{"id":1,"author":"Jan Kowalski","text":"Short message"},' +
                '{"id":2,"author":"Piotr Nowak","text":"Very long message"}]');

            scope = $rootScope.$new();
            ctrl = $controller(EntriesController, {$scope:scope});

            $rootScope.loggedUser = {
                login: "Jan Kowalski"
            };

            $httpBackend.flush();
        }));

        it('Should initialize entry text as empty', function () {
            expect(scope.entryText).toBe('');
        });

        it('Should have size and logs defined', function () {
            expect(scope.logs).toBeDefined();
            expect(scope.size).toBeDefined();
        });

        it('Should have size set to two', function () {
            expect(scope.size).toBe(2);
        });

        it("NoEntries should return false", function () {
            expect(scope.noEntries()).toBe(false);
        });

        it('Should have user logged', function () {
            expect(scope.isLogged()).toBe(true);
        });

        it("Should mark user as an owner of first entry", function () {
            expect(scope.isOwnerOf(scope.logs[0])).toBe(true);
        });

        it("Should not mark user as an owner of second entry", function () {
            expect(scope.isOwnerOf(scope.logs[1])).toBe(false);
        });

        it("Should logout user", function() {
           // Given
           $httpBackend.expectGET('rest/users/logout').respond('anything');
           expect(scope.loggedUser).not.toBe(null);

           // When
           scope.logout();
           $httpBackend.flush();

           // Then
           expect(scope.loggedUser).toBe(null);
           expect(scope.isLogged()).toBe(false);
        });
    });


    describe('with empty data response', function () {
        var scope, $httpBackend, ctrl;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/entries/count').respond('{"value":0}');
            $httpBackend.whenGET('rest/entries').respond('[{}]');

            scope = $rootScope.$new();
            ctrl = $controller(EntriesController, {$scope:scope});

            $rootScope.loggedUser = {
                login: "Jan Kowalski"
            };

            $httpBackend.flush();
        }));

        it('Should have size set to zero', function () {
            expect(scope.size).toBe(0);
        });

        it("NoEntries should return true", function () {
            expect(scope.noEntries()).toBe(true);
        });
    });

    describe('without logged user', function () {
        var scope, $httpBackend, ctrl;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/entries/count').respond('{"value":2}');
            $httpBackend.whenGET('rest/entries').respond('[{"id":1,"author":"Jan Kowalski","text":"Short message"},' +
                '{"id":2,"author":"Piotr Nowak","text":"Very long message"}]');

            scope = $rootScope.$new();
            ctrl = $controller(EntriesController, {$scope: scope});

            $httpBackend.flush();
        }));

        it('Should have user not logged', function () {
            expect(scope.isLogged()).toBe(false);
        });
    });



});
