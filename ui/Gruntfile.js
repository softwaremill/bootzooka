'use strict';

module.exports = grunt => {

  const serveStatic = require('serve-static');

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  const proxyRequests = require('grunt-connect-proxy/lib/utils').proxyRequest;
  const liveReload = require('connect-livereload')({port: 9988});

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
        tasks: ['babel', 'copy:local', 'angularFileLoader:local']
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
          middleware: connect =>  [
              serveStatic('.tmp'),
              proxyRequests,

              // Add the bit of JS to the page that connects to livereload server with WebSocket
              // and listenes for events.
              liveReload,

              connect().use('/bower_components', serveStatic('./bower_components'))
            ]
        }
      },
      dist: {
        options: {
          open: true,
          middleware: connect =>[proxyRequests, serveStatic('dist/webapp')],
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
        src: ['index.html', 'assets/img/**', '**/*.css'],
        dest: '.tmp/'
      }
    },

    clean: {
      dist: ['dist', '.tmp', '.test'],
      tmp: ['.tmp', '.test']
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
        "unused": true,
        "esnext": true
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

    wiredep: {
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
          scripts: ['.tmp/**/*.js']
        },
        src: 'dist/webapp/index.html'
      }
    },

    babel: {
      options: {
        sourceMap: false,
        presets: ['es2015']
      },
      dist: {
        files: [{
          expand: true,
          cwd: 'app/',
          src: ['**/*.js'],
          dest: '.tmp/'
        }]
      }
    }
  });

  require('matchdep').filterDev('grunt-*').forEach(dep => grunt.loadNpmTasks(dep));

  grunt.registerTask('default', () =>{
    grunt.task.run(['server']);
  });

  grunt.registerTask('server', target => {
    if (target === 'dist') {
      return grunt.task.run(['build', 'configureProxies', 'connect:dist']);
    }

    grunt.task.run([
      'clean:tmp',
      'wiredep',
      'html2js',
      'babel', //must be before copy
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
    'wiredep',
    'test:teamcity',
    'html2js',
    'copy:assets',
    'copy:index',
    'babel', // must be before concat
    'angularFileLoader',
    'useminPrepare',
    'concat:generated',
    'ngAnnotate',
    'uglify:generated',
    'cssmin:generated',
    'usemin'
  ]);

  grunt.registerTask('test', function (target) {
    let tasks = [
      'clean:tmp',
      'wiredep',
      'html2js',
      'babel',
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
    'wiredep',
    'babel',
    'html2js',
    'karma:autotest',
    'jshint'
  ]);

  grunt.loadTasks('grunttasks');
};
