---
layout: default
title:  "Frontend application"
---

Bootzooka's frontend is a true Single Page Application built with Angular. It can be treated as a completely separate application or as a client for Bootzooka server.

As a separate application it deserves its own build process handling all the details (linting, testing, minifying etc). Hence the frontend part is almost completely decoupled from server side code. The only coupling is on the level of packaging final application (which is described later in this doc).

Thanks to great tools existing in JavaScript ecosystem it is fully automated and easy to configure/extend later on.

## Installing Node.js

To work with the `ui` module you need to have `node.js` installed in version 0.10.13 or newer. Make sure you have both `node` and `npm` commands available on `PATH`.

## Installing Webpack

Bootzooka frontend project uses `nodejs` based build tool called [Webpack](https://webpack.github.io/) to automate build stuff. Bootzooka has Webpack already in dependencies so you don't need to install it globally. Every file from `./node_modules/.bin/` is on `PATH` in you `package.json` script so you don't need to provide full paths to executables. In case you would like call webpack from command line you can type `node ./node_modules/.bin/webpack`


### Why Webpack AND npm?

Webpack is a new generation module bundler and eliminates the need to use bower for frontend dependencies. Npm is a package manager; We use npm to manage the build dependencies (node modules: webpack plugins, karma test framework etc.).

## First run

If this is your first attempt to run `ui`, please go to `ui` project and run

	npm install


This will install all required dependencies for this project. If all is well you can start your development version of frontend by issuing `npm start` from command line (or running the provided `frontend-start` script in the main directory). It should start your browser and point you to [Bootzooka home page](http://0.0.0.0:9090/#/).

## Development

Build system exposes several tasks that can be run, you can find them in `package.json` file. `webpack.config.js` contains all the build configuration. 

The most important tasks exposed are:

* `npm start`
* `npm run dist`
* `npm run build`
* `npm run test`

## `npm start` task

This task serves Bootzooka application on port `9090` on `0.0.0.0` (it is available to all hosts from the same network). Your default browser should open at this location. All requests to `/api/` context for data will be proxied to port `8080` when it expects backend server to be run.

Webpack will watch for any change in frontend files (templates, js files, styles) and every change is automatically compiled (if necessary) and browser is automatically refreshed to apply changes. No need to refresh it by hand.

In this task all scripts are served in non-concatenated and non-minified version from their original locations (if possible).

## `npm run dist` task

This task is similar to the one above with one difference: it preprocessess all the files in order to create distribution (it currently includes concatenation of scripts files), runs tests and serves application from this freshly baked distribution version. This server's version doesn't watch for file changes.

## `npm run build` task

It runs all tests and builds everything to as distribution version to `dist` directory. It doesn't fire up the proxy server.

## `npm run test` task

This task runs tests and watches for changes in files. When change is detected it runs tests automatically. This is especially helpful in hard-development mode. Tests are run with Karma runner using PhantomJS as default browser. Whole tests configuration is in `karma-config.js` file in `ui` project.


## Distribution and deployment

Although in development `ui` is separate project there is no need to deploy it separately. All files from `ui/dist/webapp` (which are genereated during `npm run build`) are used by `backend` to build the final fat-jar application or `.war` package. All necessary integration with SBT (backend build) is provided. That means when you issue `package` in SBT, you get a complete web application which contains both server side and frontend components. You can drop it into your web container (as usual).
