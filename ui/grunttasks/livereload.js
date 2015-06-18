module.exports = function (grunt) {

    var http = require('http');

    grunt.registerTask('startLivereloadServer', function (target) {
        var lrserver = require('tiny-lr')();

        lrserver.listen(9988, function (err) {
            grunt.log.writeln('LR Server Started');
        });

        //var staticDirsAsRegex = new RegExp("^(" + staticDirs.join("|") + ")/");
        var staticDirsAsRegex = new RegExp("^(" + grunt.config('common').staticDirs.join("|") + ")/");

        var weAreWaiting = false;

        var waitForServer = function (timeoutMillis, onServerUp) {
            if (weAreWaiting) {
                return;
            }

            weAreWaiting = true;
            grunt.log.writeln("Starting to wait for backend server..............");
            var waitingStart = new Date().getTime();

            var options = {
                host: 'localhost',
                port: 8080,
                path: '/'
            };


            var checkServer = function () {
                http.get(options, function (resp) {
                    grunt.log.writeln("Response status from backend server: " + resp.statusCode);
                    if (resp.statusCode == 200) {
                        onServerUp();
                        weAreWaiting = false;
                    } else {
                        waitMoreIfNotTimedOut();
                    }
                }).on("error", function (err) {
                    grunt.log.writeln("Error occured on backend server connection: " + err);
                    waitMoreIfNotTimedOut();
                });
            };

            var waitMoreIfNotTimedOut = function () {
                var timePassed = new Date().getTime() - waitingStart;
                if (timePassed > timeoutMillis) {
                    grunt.log.writeln("Waiting for backend server timed out. No more waiting.");
                    weAreWaiting = false;
                } else {
                    setTimeout(checkServer, 500);
                }
            };

            setTimeout(checkServer, 1000);
        };

        grunt.event.on('watch', function (action, filepath, target) {
            if (target == 'watchAndLivereload') {
                var clientPath = filepath.replace(staticDirsAsRegex, "");
                lrserver.changed({body: {files: [clientPath]}});

            } else if (target == 'watchAndLivereloadAfterServer') {
                // Wait for backend server to reload
                waitForServer(10000, function () {
                    lrserver.changed({body: {files: ['index.html']}});
                });
            }
        });
    });
};