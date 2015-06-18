'use strict';

module.exports = function (grunt) {

    var proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
    var liveReload = require('connect-livereload')({port: 9988});
    var http = require('http');

    var staticDirs = ['app', 'tmp'];

    grunt.initConfig({

        watch: {
            templates: {
                files: ['app/*/**/*.html'],
                tasks: ['html2js']
            },
            watchAndLivereload: {
                files: [
                    'app/**/*.js',
                    'app/common/styles/*.css',
                    'tmp/scripts/**/*.js',
                    'app/*.html'
                ]
            },
            watchAndLivereloadAfterServer: {
                files: ['../backend/target/**/*']
            }
        },

        connect: {
            proxies: [{context: '/rest/', host: 'localhost', port: 8080}],
            options: {
                port: 9090,
                hostname: '0.0.0.0'
            },

            livereload: {
                options: {
                    open: true,
                    middleware: function (connect) {

                        var middlewares = [
                            proxyRequests,

                            // Add the bit of JS to the page that connects to livereload server with WebSocket
                            // and listenes for events.
                            liveReload,

                            connect().use('/bower_files', connect.static('./bower_files'))
                        ];

                        for (var i = 0, length = staticDirs.length; i < length; i++) {
                            middlewares.push(connect.static(staticDirs[i]));
                        }

                        return middlewares;
                    }
                }
            },
            dist: {
                options: {
                    open: true,
                    middleware: function (connect) {
                        return [
                            proxyRequests,
                            connect.static('dist/webapp')
                        ];
                    },
                    keepalive: true
                }
            }

        },

        html2js: {
            app: {
                options: {
                    base: 'app'
                },
                src: ['app/**/*.html', 'app/common/**/*.html'],
                dest: 'tmp/scripts/templates.js',
                module: 'smlBootzooka.templates'
            }
        },


        karma: {
            options: {
                configFile: 'karma-config.js'
            },
            autotest: {
                singleRun: false,
                autoWatch: true,
                reporters: ['progress', 'osx']
            },
            test: {
                singleRun: true
            },
            teamcity: {
                reporters: ['teamcity'],
                singleRun: true
            }
        },

        useminPrepare: {
            html: 'app/index.html',
            options: {
                dest: 'dist/webapp'
            }
        },

        uglify: {
            options: {
                compress: false,
                mangle: false
            }
        },

        usemin: {
            html: ['dist/webapp/index.html'],
            options: {
                dirs: ['dist/webapp']
            }
        },

        copy: {
            index: {
                expand: true,
                src: 'app/*.html',
                dest: 'dist/webapp',
                flatten: true
            },
            assets: {
                expand: true,
                cwd: 'app/assets',
                src: ['img/**/*'],
                dest: 'dist/webapp/assets'
            }
        },

        clean: {
            dist: ['dist', 'tmp'],
            tmp: 'tmp'
        },

        jshint: {
            options: grunt.file.readJSON('./.jshintrc'),
            app: {
                options: {
                    "globals": {
                        bootzooka: false,
                        angular: false,
                        $: false,
                        jQuery: false
                    }

                },
                files: {
                    src: ['app/**/*.js']
                }
            },
            tests: {
                options: {
                    "globals": {
                        angular: false,
                        $: false,

                        // Jasmine stuff
                        jasmine: false,
                        expect: false,
                        spyOn: false,
                        describe: false,
                        it: false,
                        beforeEach: false,
                        afterEach: false,

                        // Angular mock stuff
                        inject: false,
                        module: false
                    }

                },
                files: {
                    src: ['test/**/*.js']
                }

            }
        },
      bowerInstall: {
        target: {
          src: ['app/index.html'],
          exclude: ['bower_files/angular-mocks/angular-mocks.js']
        }
      }
    });

    require('matchdep').filterDev('grunt-*').forEach(function (dep) {
        grunt.loadNpmTasks(dep);
    });

    grunt.registerTask('server', function(target) {
        if(target === 'dist') {
            return grunt.task.run(['build', 'configureProxies', 'connect:dist']);
        }

        grunt.task.run([
          'clean:tmp',
          'bowerInstall',
          'html2js',
          'configureProxies',
          'connect:livereload',
          'startLivereloadServer',
          'watch'
        ]);
    });

    grunt.registerTask('build', [
      'clean:dist',
      'bowerInstall',
      'test:teamcity',
      'html2js',
      'copy:assets',
      'copy:index',
      'useminPrepare',
      'concat',
      'usemin'
    ]);

    grunt.registerTask('test', function(target) {
        var tasks = [
            'clean:tmp',
            'bowerInstall',
            'html2js'
        ];
        if(target === 'teamcity') {
            tasks.push('karma:teamcity');
        } else {
            tasks.push('karma:test');
        }
        grunt.task.run(tasks);
    });

    grunt.registerTask('autotest', [
        'clean:tmp',
        'bowerInstall',
        'html2js',
        'karma:autotest'
    ]);

    grunt.registerTask('startLivereloadServer', function(target) {
        var lrserver = require('tiny-lr')();

        lrserver.listen(9988, function (err) {
            grunt.log.writeln('LR Server Started');
        });

        var staticDirsAsRegex = new RegExp("^(" + staticDirs.join("|") + ")/");

        var weAreWaiting = false;

        var waitForServer = function(timeoutMillis, onServerUp) {
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


            var checkServer = function() {
                http.get(options, function (resp) {
                    grunt.log.writeln("Response status from backend server: " + resp.statusCode);
                    if (resp.statusCode == 200) {
                        onServerUp();
                        weAreWaiting = false;
                    } else {
                        waitMoreIfNotTimeouted();
                    }
                }).on("error", function(err) {
                    grunt.log.writeln("Error occured on backend server connection: " + err);
                    waitMoreIfNotTimeouted();
                });
            };

            var waitMoreIfNotTimeouted = function () {
                var timePassed = new Date().getTime() - waitingStart;
                if (timePassed > timeoutMillis) {
                    grunt.log.writeln("Waiting for backend server timeouted. No more waiting.");
                    weAreWaiting = false;
                } else {
                    setTimeout(checkServer, 100);
                }
            };

            setTimeout(checkServer, 300);
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