'use strict';

module.exports = function (grunt) {

    var proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
    var liveReload = require('connect-livereload')({port: 9988});

    grunt.initConfig({

        watch: {
            templates: {
                files: ['app/**/*.html'],
                tasks: ['html2js']
            },
            livereload: {
                options: {
                    livereload: {
                        port: 9988
                    }
                },
                files: [
                    'app/**/*.js',
                    'app/**/**/*.js',
                    'app/common/styles/*.css',
                    'tmp/scripts/**/*.js',
                    'app/*.html',
                    'app/**/*.html'
                ]
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
                        return [
                            proxyRequests,
                            liveReload,
                            connect.static('tmp'),
                            connect().use('/bower_files', connect.static('./bower_files')),
                            connect.static('app')
                        ];
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

};