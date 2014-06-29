# Bootzooka frontend application

Bootzooka's frontend is a true Single Page Application built with Angular. It can be treated as a completely separate
application or as a client for Bootzooka server.

As a separate application it deserves its own build process handling all the details (linting, testing, minifying etc).
Hence the frontend part is almost completely decoupled from server side code. The only coupling is on the level of
packaging final application (which is described later in this doc).

Thanks to great tools existing in JavaScript ecosystem it is fully automated and easy to configure/extend later on.

Installing Node.js
---

To work with `bootzooka-ui` you need to have `node.js` installed in version 0.10.13 or newer. Make sure you have both
`node` and `npm` commands available on `PATH`.

Installing Grunt.js and Bower
---

Bootzoka frontend project uses `nodejs` based build tool called [Grunt.js](http://gruntjs.com) to automate build stuff.

#### Global installation

It is advised to install Grunt.js and Bower globally. In order to do that, please run

	npm install -g grunt-cli
	
	npm install -g bower

This will install grunt command globally and make it available. Be sure to have NPM binaries on `PATH`. You may need
to add `/usr/local/share/npm/bin/` to your PATH if you have NPM installed via Homebrew.
Then you can use `grunt` command as described below.

#### Local installation

If you don't want to install Grunt.js locally, Bootzooka has it already in dependencies, so doing `npm install` as
described above should be enough. The only drawback is that in this mode `grunt` will not be available on your `PATH`.
To run grunt from local installation you should be in `bootzooka-ui` project and run it via

	./node_modules/.bin/grunt

First run
---

If this is your first attempt to run `bootzooka-ui`, please go to `bootzooka-ui` project and run

	npm install

and

	bower install


This will install all required dependencies for this project. If all is well you can start your development version
of frontend by issuing `grunt server` from command line. It should boot up your browser and point you
to [Bootzooka home page](http://0.0.0.0:9090/#/).


Development
---

Build system exposes several tasks that can be run. `Gruntfile.js` contains all the build configuration. Run it
with `grunt <task>` if you have grunt installed globally or via `./node_modules/.bin/grunt <task>` if installed
locally.

The most important tasks exposed are:

- `grunt server`
- `grunt server:dist`
- `grunt build`
- `grunt test`
- `grunt autotest`

`Grunt server` task
---

This task serves Bootzooka application on port `9090` on `0.0.0.0` (it is available to all hosts from the same network).
Your default browser should open at this location. All requests to `/rest/` context for data will be proxied to port
`8080` when it expects backend server to be run.

Grunt will watch for any change in frontend files (templates, js files, styles) and every change is automatically
compiled (if necessary) and browser is automatically refreshed to apply changes. No need to refresh it by hand.

**Note: if you have LiveReload extension enabled in you browser, please disable it so that it doesn't interfere with
Grunt's**

In this task all scripts are served in non-concatenated and non-minified version from their original locations
(if possible).

`Grunt server:dist` task
---

This task is similar to the one above with one difference: it preprocessess all the files in order to create
distribution (it currently includes concatenation of scripts files), runs tests and serves application from this
freshly baked distribution version. This server's version doesn't watch for file changes.

`Grunt build` task
---

It runs all tests and builds everything to as distribution version to `dist` directory. It doesn't fire up server.

`Grunt test` task
---

It simply tests the build one time. Tests are run with Karma runner using PhantomJS as default browser. Whole tests
configuration is in `karma-config.js` file in `bootzooka-ui` project.

`Grunt autotest` task
---

This task runs tests and watches for changes in files. When change is detected it runs tests automatically. This is
especially helpful in hard-development mode.

Distribution and deployment
---

Although in development `bootzooka-ui` is separate project there is no need to deploy it separately. All files
from `bootzooka-ui/dist/webapp` (which are genereated during `grunt build`) are used by `bootzooka-rest` to build
WAR application. All necessary integration on SBT (server build) level is already provided. That means when you issue
`package` in SBT, you get complete web application which contains both server side and frontend components. You can drop
it into your web container (as usual) and make use of it.