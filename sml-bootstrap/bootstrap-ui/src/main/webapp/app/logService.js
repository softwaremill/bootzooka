angular.module('logService', ['ngResource']).
    factory('LogService', function($resource) {
        var LogService = $resource('rest/entries/:id', { },
            {
                addNew: {method: 'POST'},
                update: {method: 'POST'}
            }
        );

        LogService.addNew = function(entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            json.author = "Anonymous";
            LogService.save(angular.toJson(json), successFunction);
        };

        LogService.load = function(logObjectId) {
            var entry = LogService.get({id: logObjectId});
            console.log(entry);
            console.log(entry.author);
            console.log(entry.toString());
            return entry;
        };

        LogService.update =  function(logObject) {
            LogService.save(logObject);
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

