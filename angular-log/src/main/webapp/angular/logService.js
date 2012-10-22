angular.module('logService', ['ngResource']).
    factory('LogService', function($resource) {
        var LogService = $resource('/logs', { },
            {
                addNew: {method: 'POST'}
            }
        );

        LogService.loadLogs = function(cb) {
            return LogService.query();
        }

        LogService.addNew = function(entryText) {
            var json = new Object()
            json.text = entryText;
            json.author = "Anonymous"
            var date = new Date();
            json.date = date.getDate()+"/" + (date.getMonth()+1) +"/"+ date.getFullYear() +
                " " + date.getHours() +":" + date.getMinutes()
            LogService.save(angular.toJson(json))
        }

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

