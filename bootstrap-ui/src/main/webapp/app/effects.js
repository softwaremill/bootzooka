function showMessage( text) {
    $("#feedback").html(text)
    $("#feedback").removeClass("hidden")
    $("#feedback").fadeIn()

    setTimeout(function() {
        $("#feedback").fadeOut()
    }, 3000)
}