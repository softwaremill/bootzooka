---
layout: default
title:  "Getting started"
---

## Prerequisites

In order to build and develop on Bootzooka foundations you need the following:

* Java JDK >= 11
* [sbt](http://www.scala-sbt.org/) >= 1.2
* Node.js >= 10.0 (We recommend [nvm](https://github.com/creationix/nvm) - node version manager)
* PostgreSQL

## How to run

Because (as said before) Bootzooka consists of two separate applications, in development you need to run both separately. This way the server-side code can be reloaded independently of the frontend code: if, for example, you make a small change to an HTML file, thanks to live-reload you'll see the changes immediately, rebuilding and reloading only the frontend part, while the server is running undisturbed.

**NOTE: This is not the case in production by default. When the final fat-jar application package is built it contains both client and server parts.**

### Database

Bootzooka uses [PostgreSQL](https://www.postgresql.org) to store data, so you will need the database running to use the application. By default, Bootzooka uses the `bootzooka` database using the `postgres` user (empty password), connecting to a server running on `localhost:5432`. This can be customised in the `application.conf` file.

You can either use a stand-alone database, a docker image (see the `docker-compose.yml` file), or any other PostgreSQL instance.

### Server

To run the backend server part, enter the main directory and type `./backend-start.sh` or `backend-start.bat` depending on your OS.

### Browser client

To run the frontend server part, enter the main directory and type `./frontend-start.sh`. This should open `http://localhost:3000/` in your browser (frontend listens on port 3000, backend on port 8080; so all backend HTTP requests will be proxied to port 8080).

For details of frontend build and architecture please refer to the [frontend docs](frontend.html).
