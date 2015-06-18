'use strict';

module.exports = function (grunt) {

    var proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
    var liveReload = require('connect-livereload')({port: 9988});

    grunt.initConfig({

        common: {
            staticDirs: ['app', '.tmp']
        },

        watch: {
            templates: {
                // Watch only templates, don't watch app/index.html. All the templates are in subdirectories, not in
                // the root 'app' dir.
                files: ['app/*/**/*.html'],
                tasks: ['html2js']
            },
            resources: {
                files: [
                    'app/**/*.js',
                    'app/common/styles/*.css',
                    'app/*.html'
                ],
                tasks: ['copy:local', 'angularFileLoader:local']
            },
            watchAndLivereload: {
                files: [
                    '.tmp/**/*'
                ]
            },
            watchAndLivereloadAfterServer: {
                files: ['../backend/target/compilationFinished']
            }
        },

        connect: {
            proxies: [{context: '/api/', host: 'localhost', port: 8080}],
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

                            connect().use('/bower_components', connect.static('./bower_components'))
                        ];
                        middlewares.push(connect.static('.tmp'));
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
                src: ['app/**/*.html'],
                dest: '.tmp/scripts/templates.js',
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
            html: 'dist/webapp/index.html',
            options: {
                dest: 'dist/webapp'
            }
        },

        ngAnnotate: {
            options: {
                singleQuotes: true
            },
            app: {
                files: [
                    {
                        expand: true,
                        src: ['.tmp/concat/scripts/bootzooka-all.js']
                    }
                ]
            }
        },

        uglify: {
            options: {
                compress: true,
                mangle: true
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
            },
            local: {
                expand: true,
                cwd: 'app/',
                src: ['**/*.js', 'index.html', 'assets/img/**', '**/*.css'],
                dest: '.tmp/'
            }
        },

        clean: {
            dist: ['dist', '.tmp'],
            tmp: '.tmp'
        },

        jshint: {
            options: {
                "sub": true,
                "curly": true,
                "eqeqeq": true,
                "eqnull": true,
                "expr": true,
                "noarg": true,
                "node": true,
                "trailing": true,
                "undef": true,
                "unused": true
            },
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
                exclude: ['bower_components/angular-mocks/angular-mocks.js']
            }
        },

        angularFileLoader: {
            local: {
                options: {
                    scripts: ['.tmp/**/*.js']
                },
                src: '.tmp/index.html'
            },
            default_options: {
                options: {
                    scripts: ['app/**/*.js', '.tmp/scripts/**/*.js']
                },
                src: 'dist/webapp/index.html'
            }
        }
    });

    require('matchdep').filterDev('grunt-*').forEach(function (dep) {
        grunt.loadNpmTasks(dep);
    });

    grunt.registerTask('server', function (target) {
        if (target === 'dist') {
            return grunt.task.run(['build', 'configureProxies', 'connect:dist']);
        }

        grunt.task.run([
            'clean:tmp',
            'bowerInstall',
            'html2js',
            'copy:local',
            'angularFileLoader:local',
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
        'angularFileLoader',
        'useminPrepare',
        'concat:generated',
        'ngAnnotate',
        'uglify:generated',
        'cssmin:generated',
        'usemin'
    ]);

    grunt.registerTask('test', function (target) {
        var tasks = [
            'clean:tmp',
            'bowerInstall',
            'html2js',
            'jshint'
        ];
        if (target === 'teamcity') {
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
        'karma:autotest',
        'jshint'
    ]);

    grunt.loadTasks('grunttasks');
};