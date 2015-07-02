'use strict';

module.exports = function(config) {

    var files = [];

    [
        'bower_components/jquery/dist/jquery.js',
        'bower_components/blockui/jquery.blockUI.js',
        'bower_components/bootstrap/dist/js/bootstrap.js',
        'bower_components/angular/angular.js',
        'bower_components/angular-resource/angular-resource.js',
        'bower_components/angular-ui-router/release/angular-ui-router.js',
        'bower_components/angular-cookies/angular-cookies.js',
        'bower_components/angular-sanitize/angular-sanitize.js',
        'bower_components/angular-mocks/angular-mocks.js',
        'bower_components/underscore/underscore.js',
        'bower_components/moment/moment.js'
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
