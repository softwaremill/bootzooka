
var entriesModel;
var sizeModel;
var uptimeModel;


ko.validation.configure({
    registerExtenders: true,
    messagesOnModified: true,
    insertMessages: true,
    parseInputAttributes: true,
    messageTemplate: null
});


function loadEntries() {

    $.get("/logs", function(data) {
        console.log(data);
        if(entriesModel == null) {
            entriesModel = new Object();
            entriesModel.newEntry = ko.observable()
                .extend({
                    required: true,
                    minLength: 5,
                    maxLength: 140 }
            );
            entriesModel.entries = ko.mapping.fromJS(data)
            entriesModel.errors = ko.validation.group(entriesModel);
            ko.applyBindings(entriesModel, document.getElementById("entriesDiv"));
        }
        else {
            ko.mapping.fromJS(data, entriesModel.entries);
        }
    });
}

function getCount() {
    $.get("/logs/logs-count", function(data) {
        if(sizeModel == null) {
            sizeModel = new Object();
            sizeModel.size = ko.mapping.fromJS(data);
            ko.applyBindings(sizeModel, document.getElementById("sizeDiv"));
        }
        else {
            ko.mapping.fromJS(data, sizeModel.size);
        }
    });
}


function getUptime() {
    $.get("/uptime", function(data) {
        if(uptimeModel == null) {
            uptimeModel = new Object();
            uptimeModel.uptime = ko.mapping.fromJS(data);
            ko.applyBindings(uptimeModel, document.getElementById("uptimeDiv"));
        }
        else {
            ko.mapping.fromJS(data, uptimeModel.uptime);
        }
    });
}

function addEntry() {

    if (entriesModel.errors().length > 0) {
        entriesModel.errors.showAllMessages();
    }
    else {
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
                getCount();
                getUptime();
                entriesModel.newEntry("");
            }
        });
    }

    return false
}

function deleteEntry(entry) {

    $.ajax({
        url: "logs/" + entry.id(),
        type: "DELETE",
        dataType: "text",
        contentType: "application/json",
        success: function() {
            loadEntries();
            getCount();
            getUptime();
        }
    });

    return false;
}


$(function() {
    console.log("Init...");
    loadEntries();
    getCount();
    getUptime();
})