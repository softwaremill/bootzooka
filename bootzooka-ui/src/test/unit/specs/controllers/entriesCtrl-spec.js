'use strict';

describe("Entries Controller", function () {

    beforeEach(module('smlBootzooka.entries'));

    afterEach(inject(function(_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    describe('with not-empty data response and logged user', function () {
        var scope, $httpBackend, ctrl, userSessionService;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, $cookies, UserSessionService) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/entries/count').respond('{"value":2}');
            var timestamp = 1356969850745;
            $httpBackend.whenGET('/rest/entries/count-newer/' + timestamp ).respond('{"value":5}');
            $httpBackend.whenGET('rest/entries').respond('{"entries":[{"id":1,"author":"Jan Kowalski","text":"Short message"},' +
                '{"id":2,"author":"Piotr Nowak","text":"Very long message"}], "timestamp": ' + timestamp + '}');

            scope = $rootScope.$new();
            ctrl = $controller('EntriesCtrl', {$scope:scope});

            userSessionService = UserSessionService;
            userSessionService.loggedUser = {
                login: "Jan Kowalski"
            };
            $cookies["scentry.auth.default.user"] = "Jan Kowalski";

            $httpBackend.flush();
        }));

        it('Should initialize entry text as empty', function () {
            expect(scope.entryText).toBe('');
        });

        it('Should have size and logs defined', function () {
            expect(scope.logs).toBeDefined();
            expect(scope.size).toBeDefined();
            expect(scope.numberOfNewEntries).toBeDefined();
            expect(scope.lastLoadedTimestamp).toBeDefined();
        });

        it('Should have size set to two', function () {
            expect(scope.size).toBe(2);
        });

        it("NoEntries should return false", function () {
            expect(scope.noEntries()).toBe(false);
        });

        it("Should mark user as an owner of first entry", function () {
            expect(scope.isOwnerOf(scope.logs[0])).toBe(true);
        });

        it("Should not mark user as an owner of second entry", function () {
            expect(scope.isOwnerOf(scope.logs[1])).toBe(false);
        });

        it("Should set number of new entries", function() {
            // When
            ctrl.checkForNewEntries();
            $httpBackend.flush();

            //Then
            expect(scope.numberOfNewEntries).toBe(5);
        })

        it("should fill entry with date on page", function() {
            // given
            $httpBackend.expectGET('rest/entries').respond('{"entries":[{"id":1,"author":"Jan Kowalski","text":"Short message","entered":"2012-01-01 12:12:12"}], "timestamp":11111}');

            // when
            scope.reloadEntries();
            $httpBackend.flush();

            // then
            expect(scope.logs[0].entered).toBe("2012-01-01 12:12:12");
        });

        it("should set timestamp when entries are loaded", function() {
            // given
            var timestamp = 11111111;
            $httpBackend.expectGET('rest/entries').respond('{"entries":[{"id":1,"author":"Jan Kowalski","text":"Short message","entered":"2012-01-01 12:12:12"}],' +
                ' "timestamp": '+ timestamp +'}');

            // when
            scope.reloadEntries();
            $httpBackend.flush();

            // then
            expect(scope.lastLoadedTimestamp).toBe(timestamp);
        });
    });


    describe('with empty data response', function () {
        var scope, $httpBackend, ctrl;

        beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, UserSessionService) {
            $httpBackend = _$httpBackend_;
            $httpBackend.whenGET('/rest/entries/count').respond('{"value":0}');
            $httpBackend.whenGET('rest/entries').respond('{"entries":[],"timestamp":1356970017145}');

            scope = $rootScope.$new();
            ctrl = $controller('EntriesCtrl', {$scope:scope});

            UserSessionService.loggedUser = {
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
});
