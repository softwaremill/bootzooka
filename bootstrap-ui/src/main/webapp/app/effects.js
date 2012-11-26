function showInfoMessage( text) {
    $("#feedback").html(text);
    $("#feedback").addClass("info");
    $("#feedback").removeClass("hidden");
    $("#feedback").removeClass("error");
    $("#feedback").fadeIn();

    setTimeout(function() {
        $("#feedback").fadeOut()
    }, 3000)
}

function showErrorMessage(text) {
    $("#feedback").html(text);
    $("#feedback").addClass("error");
    $("#feedback").removeClass("hidden");
    $("#feedback").removeClass("info");
    $("#feedback").fadeIn();
}