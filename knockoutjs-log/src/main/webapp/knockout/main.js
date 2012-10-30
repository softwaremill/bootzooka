

var entriesModel = {
    entries: ko.observableArray([]),
    size: ko.observable(0),
    uptime: ko.observable(0),
    newEntry: ko.observable(""),
    init: false
}

function loadEntries() {

    $.get("/logs", function(data) {
        console.log(data);
        entriesModel.entries = ko.mapping.fromJS(data);
        console.log("model = " + entriesModel.entries());
        if(entriesModel.init === false) {
            ko.applyBindings(entriesModel);
            entriesModel.init = true;
            console.log("ApplyBinding executed");
        }
    });

}

function getCount() {
    $.get("/logs/logs-count", function(data) {
        console.log("count =" + data);
        entriesModel.size = ko.mapping.fromJS(data).value;
    });
}


function getUptime() {
    $.get("/uptime", function(data) {
        entriesModel.uptime = ko.mapping.fromJS(data).value;
        console.log("uptime =" + entriesModel.uptime());
    });
}

function addEntry() {
    console.log("Button clicked");
    console.log(entriesModel.newEntry());


    var value = new Object();
    value.text = entriesModel.newEntry();
    value.author = "Anonymous";
    var date = new Date();
    value.date = date.getDate()+"/" + (date.getMonth()+1) +"/"+ date.getFullYear() +
        " " + date.getHours() +":" + date.getMinutes();
    var json = ko.mapping.toJSON(value);

    console.log("json =" + json);

    $.ajax({
        url: "logs",
        type: "POST",
        dataType: "text",
        contentType: "application/json",
        data: json,
        success: function(){
            loadEntries();
        }
    });

    return false
}


$(function() {
    console.log("Init...");
    loadEntries();
//    getCount();
//    getUptime();
})