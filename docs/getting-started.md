---
layout: default
title: "Getting started"
---

## Prerequisites

In order to build and develop a Bootzooka-derived application you'll need the following:

- Java JDK >= 21
- [sbt](http://www.scala-sbt.org/) >= 1.10
- Node.js >= 22 (We recommend [nvm](https://github.com/nvm-sh/nvm) - node version manager)
- PostgreSQL

## How to run

Because (as said before) Bootzooka consists of two separate applications, in development you need to run both
separately. This way the server-side code can be reloaded independently of the frontend code: if, for example, you make
a small change to an HTML file, thanks to live-reload you'll see the changes immediately, rebuilding and reloading only
the frontend part, while the server is running undisturbed.

**NOTE: This is not the case in production by default. When the final Docker image is built it contains both client and
server parts.**

### Database

Bootzooka uses [PostgreSQL](https://www.postgresql.org) to store data, so you will need the database running to use the
application. By default, Bootzooka uses the `bootzooka` database using the `postgres` user, connecting to a server
running on `localhost:5432`. This can be customised in the `application.conf` file.

You can either use a stand-alone database, a docker image, or any other PostgreSQL instance.

### Backend

To run the backend server part, enter the main directory and type `./backend-start.sh` or `backend-start.bat` depending
on your OS.

By default, when using the above startup script, OpenTelemetry is disabled to avoid telemetry export exceptions, as you
might not have a collector running. If you do have one, edit the startp script appropriately.

The backend will start on [`http://localhost:8080`](http://localhost:8080). You can explore the API docs using the
Swagger UI by navigating to [`http://localhost:8080/api/v1/docs`](http://localhost:8080/api/v1/docs).

When any source files change on the backend, it will be automatically restarted. Moreover, if there are new or changed
endpoint definitions, the OpenAPI description will be regenerated, which is then used by the frontend to generate
service stubs.

### Frontend

First, create a `ui/.env` file, using the `ui/.env.example`. Unless you changed the port of the backend, the default
value will be fine.

Then, use `./frontend-start.sh` to start the frontend in development mode. This should open `http://localhost:8081/` in
your browser.

For details of frontend build and architecture please refer to the [frontend docs](frontend.html).
