angular.module('entriesService', ['ngResource']).
    factory('EntriesService', function($resource) {
        var entriesService = $resource('rest/entries/:id', { }, {

            insert: { method: "PUT"} }
        );

        entriesService.loadAll = function(successFunction) {
            entriesService.query(null, successFunction);
        };

        entriesService.addNew = function (entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            entriesService.insert(angular.toJson(json), successFunction);
        };

        entriesService.load = function(logObjectId, successFunction) {
            entriesService.get({id: logObjectId}, successFunction);
        };

        entriesService.update =  function(logObject) {
            var json = new Object();
            json.text = logObject.text;
            json.id = logObject.id;
            entriesService.save(angular.toJson(json));
        };

        entriesService.deleteEntry = function(logObjectId, successFunction) {
            entriesService.remove({id: logObjectId}, successFunction);
        };

        return entriesService;
    });

angular.module('entriesCounterService', ['ngResource']).
    factory("EntriesCounterService", function($resource) {
        var entriesCounterService = $resource("/rest/entries/count");

        entriesCounterService.countLogs = function(successFunction) {
            entriesCounterService.get(successFunction);
        };

        return entriesCounterService;
    });

