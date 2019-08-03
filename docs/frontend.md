---
layout: default
title:  "Frontend application"
---

Bootzooka's frontend is a true Single Page Application built with React. It can be treated as a completely separate application or as a client for Bootzooka server.

As a separate application it deserves its own build process handling all the details (linting, testing, minifying etc). Hence the frontend part is almost completely decoupled from server side code. The only coupling is on the level of packaging final application (which is described later in this doc).

Please note, that the UI is based on [fantastic tool called Create React App](https://github.com/facebook/create-react-app) which takes care of fine details like build configuration, minification & hot reloading under the hood, without you having to worry about it. For more details, see the project's [page](https://github.com/facebook/create-react-app).

## Installing Node.js & Yarn

To work with the `ui` module you need to have `node.js` installed in version 12.0 or newer. Make sure you have `node` command available on `PATH`.

As a package manager, Bootzooka's UI uses [Yarn](https://yarnpkg.com). Make sure to have it installed before the first run.

## First run

If this is your first attempt to run `ui`, please go to `ui` project and run

	yarn install

This will install all required dependencies for this project. If all is well you can start your development version of frontend by issuing `yarn start` from command line (or running the provided `frontend-start` script in the main directory). It should start your browser and point you to [Bootzooka home page](http://0.0.0.0:3000/#/).

## Development

Build system exposes several tasks that can be run, you can find them in `package.json` file.

The most important tasks exposed are:

* `yarn start`
* `yarn build`
* `yarn test`
* `yarn test:ci`

## `yarn start` task

This task serves Bootzooka application on port `3000` on `0.0.0.0` (it is available to all hosts from the same network). Your default browser should open at this location. All requests to the backend will be proxied to port `8080` where it expects the server to be run.

Hot reload is in place already (provided by the Create React App stack), so every change is automatically compiled (if necessary) and browser is automatically refreshed to apply changes. No need to refresh it by hand.

In this task all scripts are served in non-concatenated and non-minified version from their original locations (if possible).

## `yarn build` task

It builds everything as a distribution-ready version to `dist` directory. It doesn't fire up the proxy server.

## `yarn test` task

This task runs tests and watches for changes in files. When change is detected it runs tests automatically. This is especially helpful in hard-development mode. Tests are run with Jest using Enzyme.

## `yarn test` task

This task runs tests just once (useful in CI environments, where an exit code is required).

## Distribution and deployment

Although in development `ui` is separate project there is no need to deploy it separately. All files from `ui/dist/webapp` (which are generated during `yarn build`) are used by `backend` to build the final fat-jar application. All necessary integration with SBT (backend build) is provided. That means when you issue `package` in SBT, you get a complete web application which contains both server side and frontend components.
