'use strict';

module.exports = function(config) {

    var files = [];

    [
        'jquery-1.8.2-min.js',
        'jquery-blockui.min.js',
        'bootstrap-2.2.2.js',
        'angular-1.2.6/angular.js',
        'angular-1.2.6/angular-resource.js',
        'angular-1.2.6/angular-route.js',
        'angular-1.2.6/angular-cookies.js',
        'angular-1.2.6/angular-sanitize.js',
        'angular-1.2.6/angular-mocks.js'
    ].forEach(function(file) {
        files.push('app/vendor/' + file);
    });

    files.push('app/scripts/*.js');
    files.push('app/scripts/**/*.js');
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
