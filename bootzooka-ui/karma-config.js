'use strict';

module.exports = function(config) {

    var files = [];

    [
        'bower_files/jquery/dist/jquery.js',
        'bower_files/blockui/jquery.blockUI.js',
        'bower_files/bootstrap/dist/js/bootstrap.js',
        'bower_files/angular/angular.js',
        'bower_files/angular-resource/angular-resource.js',
        'bower_files/angular-route/angular-route.js',
        'bower_files/angular-cookies/angular-cookies.js',
        'bower_files/angular-sanitize/angular-sanitize.js',
        'bower_files/angular-mocks/angular-mocks.js'
    ].forEach(function(file) {
        files.push(file);
    });

    files.push('app/app.js');
    files.push('app/**/*.js');
    files.push('app/**/**/*.js');
    files.push('tmp/scripts/**/*.js');
    files.push('test/**/*.js');

    config.set({
        basePath: '',
        frameworks: ['jasmine'],
        files: files,
        exclude: [],
        port: 7070,
        logLevel: config.LOG_INFO,
        autoWatch: false,

        // Start these browsers, currently available:
        // - Chrome
        // - ChromeCanary
        // - Firefox
        // - Opera
        // - Safari (only Mac)
        // - PhantomJS
        // - IE (only Windows)
        browsers: ['PhantomJS']
    });
};
