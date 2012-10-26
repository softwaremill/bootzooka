angular.module('logService', ['ngResource']).
    factory('LogService', function($resource) {
        var LogService = $resource('/logs/:id', { },
            {
                addNew: {method: 'POST'}
            }
        );

        LogService.addNew = function(entryText, successFunction) {
            var json = new Object();
            json.text = entryText;
            json.author = "Anonymous"
            var date = new Date();
            json.date = date.getDate()+"/" + (date.getMonth()+1) +"/"+ date.getFullYear() +
                " " + date.getHours() +":" + date.getMinutes()
            var refreshedList = null;
            LogService.save(angular.toJson(json), successFunction)
        }

//        LogService.deleteEntry = function(logObjectId, successFunction) {
//            var json = new Object();
//            json.value = logObjectId;
//            LogService.remove(angular.toJson(json), successFunction);
//        }

        LogService.deleteEntry = function(logObjectId, successFunction) {
            console.log("delete " + logObjectId)
            return LogService.remove({id: logObjectId}, successFunction);
        };

        return LogService;
    });

angular.module('logCounterService', ['ngResource']).
    factory("LogCounterService", function($resource) {
        var LogCounterService = $resource("/logs/logs-count");

        LogCounterService.countLogs = function(cb) {
            return LogCounterService.get();
        }

        return LogCounterService;
    });

