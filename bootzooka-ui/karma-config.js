'use strict';

module.exports = function(config) {

    var files = [];

    [
        'jquery.min.js',
        'jquery-blockui.js',
        'bootstrap.js',
        'angular.js',
        'angular-resource.js',
        'angular-route.js',
        'angular-cookies.js',
        'angular-sanitize.js',
        'angular-mocks.js'
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
        browsers: ['Chrome']
//        browsers: ['PhantomJS']
    });
};
