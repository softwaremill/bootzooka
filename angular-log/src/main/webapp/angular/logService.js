angular.module('logService', ['ngResource']).
    factory('LogService', function($resource) {
        var LogService = $resource('/logs/:id', { },
            {
                addNew: {method: 'POST'},
                update: {method: 'POST'}
            }
        );

        LogService.addNew = function(entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            json.author = "Anonymous";
            var date = new Date();
            json.date = date.getDate()+"/" + (date.getMonth()+1) +"/"+ date.getFullYear() +
                " " + date.getHours() +":" + date.getMinutes();
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
        var LogCounterService = $resource("/logs/logs-count");

        LogCounterService.countLogs = function(cb) {
            return LogCounterService.get();
        };

        return LogCounterService;
    });

