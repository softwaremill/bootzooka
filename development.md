---
layout: default
title:  "Development"
---

## Prerequisities

In order to build and develop on Bootzooka foundations you need the following:

* Java JDK >= 8
* [SBT](http://www.scala-sbt.org/) >= 0.13
* Node.js >= 5.0 (We recommend [NVM](https://github.com/creationix/nvm) - node version manager)

## How to run

Because (as said before) Bootzooka consists of two separate applications, in development you need to run both separately. This way the server-side code can be reloaded independently of the frontend code: if, for example, you make a small change to an HTML file, thanks to live-reload you'll see the changes immediately, rebuilding and reloading only the frontend part, while the server is running undisturbed.

**NOTE: This is not the case in production by default. When the final fat-jar application package is built it contains both client and server parts.**

### Server

To run the backend server part, enter the main directory and type `./backend-start.sh` or `backend-start.bat` depending on your OS.

### Browser client

To run the frontend server part, enter the main directory and type `./frontend-start.sh`. This should open `http://0.0.0.0:9090/` in your browser (frontend listens on port 9090, backend on port 8080; so all HTTP requests will be proxied to port 8080).

For details of frontend build and architecture please refer to the [frontend docs](frontend.html).
