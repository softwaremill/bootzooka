angular.module('entriesService', ['ngResource']).
    factory('EntriesService', function($resource) {

        var entriesService = new Object();

        entriesService.crudService = $resource('rest/entries/:id', { }, {
            insert: { method: "PUT"} }
        );

        entriesService.counter = $resource("/rest/entries/count");


        entriesService.loadAll = function(successFunction) {
            entriesService.crudService.query(null, successFunction);
        };

        entriesService.addNew = function (entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            entriesService.crudService.insert(angular.toJson(json), successFunction);
        };

        entriesService.load = function(logObjectId, successFunction) {
            entriesService.crudService.get({id: logObjectId}, successFunction);
        };

        entriesService.update =  function(logObject) {
            var json = new Object();
            json.text = logObject.text;
            json.id = logObject.id;
            entriesService.crudService.save(angular.toJson(json));
        };

        entriesService.deleteEntry = function(logObjectId, successFunction) {
            entriesService.crudService.remove({id: logObjectId}, successFunction);
        };

        entriesService.count = function(successFunction) {
            entriesService.counter.get(successFunction);
        };

        return entriesService;
    });
