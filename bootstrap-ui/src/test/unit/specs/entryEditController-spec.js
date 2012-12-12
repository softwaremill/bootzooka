'use strict';

describe("EntryEdit Controller", function () {

    beforeEach(module('smlBootstrap.services', 'smlBootstrap.controllers'));

    afterEach(inject(function (_$httpBackend_) {
        _$httpBackend_.verifyNoOutstandingExpectation();
        _$httpBackend_.verifyNoOutstandingRequest();
    }));

    var scope, $httpBackend, ctrl, userSessionService, location;

    beforeEach(inject(function (_$httpBackend_, $rootScope, $routeParams, $controller, UserSessionService, $location) {
        $httpBackend = _$httpBackend_;
        $httpBackend.whenGET('rest/entries/1').respond('{"id":"1","text":"Message!","author":"admin"}');

        //FIXME lukaszlenart: because when EntryService is created it calls backend to retrieve entries - this should be changed
        $httpBackend.whenGET('rest/entries').respond('[]');

        scope = $rootScope.$new();

        ctrl = $controller('EntryEditController', {$scope: scope});

        UserSessionService.loggedUser = {
            login: 'admin'
        };

        $routeParams.entryId = 1;

        location = $location;

        $httpBackend.flush();
    }));

    it('should check if logged in user is owner of entry and can edit it', function () {
        // given
        scope.log = {
            author: 'admin'
        };

        // when
        var isOwnerOfEntry = scope.isOwnerOfEntry();

        // then
        expect(isOwnerOfEntry).toBe(true);
    });

    it('should update entry', function () {
        // given
        $httpBackend.expectPUT('rest/entries', '{"text":"New message","id":"1"}').respond('nothing');
        scope.log = {
            text: 'New message',
            id: "1"
        };

        // when
        scope.updateEntry();
        $httpBackend.flush();

        // then
    });

    it('should navigate to root after cancel', function() {
        // when
        scope.cancelEdit();

        // then
        expect(location.path()).toBe("/")
    });

});
