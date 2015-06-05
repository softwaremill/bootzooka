angular.module("smlBootzooka.common.filters")

    .filter("relativeDate", function() { // relative date format e.g. "4 days ago"
        return function(value) {
            return moment(value).fromNow();
        };
    })

    .filter("utcDateOnly", function() {
        return function(value) {
            return moment(value).utc().format("MMMM Do, YYYY");
        };
    });