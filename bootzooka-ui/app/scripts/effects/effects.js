(function(global) {

    var utils = {};

    utils.showInfoMessage = function(text) {
        $('#info-message').html(text);
        $('.alert-info').fadeIn(600);
        setTimeout(function () {
            $('.alert-info').fadeOut(1200);
        }, 2000);
    };

    utils.showErrorMessage = function(text) {
        $('#error-message').html(text);
        $('.alert-error').fadeIn(600);
        setTimeout(function () {
            $('.alert-error').fadeOut(1200);
        }, 4000);
    };

    global.bootzooka = global.bootzooka || {};
    global.bootzooka.utils = utils;

})(this); // this points to "window"