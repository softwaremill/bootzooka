angular.module('entriesService', ['ngResource']).
    factory('EntriesService', function($resource) {
        var EntriesService = $resource('rest/entries/:id', { }, {

            insert: { method: "PUT"} }
        );

        EntriesService.addNew = function (entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            EntriesService.insert(angular.toJson(json), successFunction);
        };

        EntriesService.load = function(logObjectId) {
            return EntriesService.get({id: logObjectId});
        };

        EntriesService.update =  function(logObject) {
            var json = new Object();
            json.text = logObject.text;
            json.id = logObject.id;
            EntriesService.save(angular.toJson(json));
        };

        EntriesService.deleteEntry = function(logObjectId, successFunction) {
            return EntriesService.remove({id: logObjectId}, successFunction);
        };

        return EntriesService;
    });

angular.module('entriesCounterService', ['ngResource']).
    factory("EntriesCounterService", function($resource) {
        var EntriesCounterService = $resource("/rest/entries/count");

        EntriesCounterService.countLogs = function(cb) {
            return EntriesCounterService.get();
        };

        return EntriesCounterService;
    });

