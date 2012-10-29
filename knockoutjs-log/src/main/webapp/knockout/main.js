

var entriesModel;

function loadEntries() {

    $.get("/logs", function(data) {
        entriesModel = {
            entries: null,
            size: 0,
            uptime: 0
        }

        console.log(data);
        entriesModel.entries = ko.mapping.fromJS(data);
        console.log("model = " + entriesModel);
        getCount();
    });
}

function getCount() {
    $.get("/logs/logs-count", function(data) {
        console.log("count =" + data);
        entriesModel.size = ko.mapping.fromJS(data).value;
        getUptime();
    });
}


function getUptime() {
    $.get("/uptime", function(data) {
        entriesModel.uptime = ko.mapping.fromJS(data).value;
        console.log("uptime =" + entriesModel.uptime());
        ko.applyBindings(entriesModel);
    });
}


$(function() {
    loadEntries();
})