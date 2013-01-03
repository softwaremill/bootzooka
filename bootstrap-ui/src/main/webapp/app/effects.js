"use strict";

function showInfoMessage(text) {
    $('#info-message').html(text);
    $('.alert-info').fadeIn(600);
    setTimeout(function () {
        $('.alert-info').fadeOut(1200);
    }, 2000);
}

function showErrorMessage(text) {
    $('#error-message').html(text);
    $('.alert-error').fadeIn(600);
    setTimeout(function () {
        $('.alert-error').fadeOut(1200);
    }, 4000);
}