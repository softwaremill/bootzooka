angular.module('logService', ['ngResource']).
    factory('LogService', function($resource) {
        var LogService = $resource('rest/entries/:id', { }, {

            insert: { method: "PUT"} }
        );

        LogService.addNew = function (entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            LogService.insert(angular.toJson(json), successFunction);
        };

        LogService.load = function(logObjectId) {
            return LogService.get({id: logObjectId});
        };

        LogService.update =  function(logObject) {
            var json = new Object();
            json.text = logObject.text;
            json.id = logObject.id;
            LogService.save(angular.toJson(json));
        };

        LogService.deleteEntry = function(logObjectId, successFunction) {
            return LogService.remove({id: logObjectId}, successFunction);
        };

        return LogService;
    });

angular.module('logCounterService', ['ngResource']).
    factory("LogCounterService", function($resource) {
        var LogCounterService = $resource("/rest/entries/count");

        LogCounterService.countLogs = function(cb) {
            return LogCounterService.get();
        };

        return LogCounterService;
    });

