module.exports = function (grunt) {

    var lrserver = require('tiny-lr')();
    var http = require('http');

    grunt.registerTask('startLivereloadServer', function (target) {
        // Run livereload server. The page connects to it via WebSocket, and it sends events on change for the
        // page to reload.
        lrserver.listen(9988, function (err) {
            grunt.log.writeln('LR Server Started');
        });

        var staticDirsAsRegex = new RegExp("^(" + grunt.config('common').staticDirs.join("|") + ")/");

        var weAreWaiting = false;

        var waitForServer = function (timeoutMillis, onServerUp) {
            // Do not enter waiting loop if we are already waiting.
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

            // Try to connect to server and run onServerUp() if response has status 200,
            // or wait a bit and try again if it has not.
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

            // Check if timeout already occured, and if not, wait a bit and run checkServer again.
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
                // If frontend file has changed, run reload immediately
                var clientPath = filepath.replace(staticDirsAsRegex, "");
                lrserver.changed({body: {files: [clientPath]}});

            } else if (target == 'watchAndLivereloadAfterServer') {
                // If backend file has changed, wait for recompilation and
                // backend server reload.
                waitForServer(10000, function () {
                    lrserver.changed({body: {files: ['index.html']}});
                });
            }
        });
    });
};