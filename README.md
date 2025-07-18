![Bootzooka](https://github.com/softwaremill/bootzooka/raw/master/banner.png)

[![CI](https://github.com/softwaremill/bootzooka/workflows/Bootzooka%20CI/badge.svg)](https://github.com/softwaremill/bootzooka/actions?query=workflow%3A%22Bootzooka+CI%22)

Bootzooka is a scaffolding project to allow quick start of development of a microservice or a web application. If you'd
like to have a jump start developing a Scala-based project, skipping the boring parts and focusing on the real business
value, this template might be for you!

You can start testing or developing Bootzooka right away with the below setup, or proceed to the mor ecomplete
[Bootzooka documentation](http://softwaremill.github.io/bootzooka/).

# Run locally using Docker

If you'd like to see the project in action, the fastest way is to use the provided Docker compose setup. It starts three
images: Bootzooka itself (either locally built or downloaded), PostgreSQL server and Graphana LGTM for observability.

# Run locally for development

If you'd like to modify some of Bootzooka's parts, or develop your own application using the template, you'll need to
start the backend & frontend in development modes separately.

## Database

First, you'll need a PostgreSQL database running. One of the options is to start one using Docker; here a `bootzooka`
database will be created:

```sh
# use "bootzooka" as a password
docker run --name bootzooka-postgres -p 5432:5432 -e POSTGRES_PASSWORD=bootzooka -e POSTGRES_DB=bootzooka -d postgres
```

## Backend

Then, you can start the backend. You'll need the JVM 21+ and [SBT](https://www.scala-sbt.org) installed:

```sh
SQL_PASSWORD=bootzooka ./backend-start.sh
```

By default, OpenTelemetry is disabled to avoid telemetry export exceptions (which is available and explorable if you are
using the Docker compose setup). If you have a collector running, edit the startp script appropriately.

The backend will start on [`http://localhost:8080`](http://localhost:8080). You can explore the API docs using the
Swagger UI by navigating to [`http://localhost:8080/api/v1/docs`](http://localhost:8080/api/v1/docs).

When any source files change on the backend, it will be automatically restarted. Moreover, if there are new or changed
endpoint definitions, the OpenAPI description will be regenerated, which is then used by the frontend to generate
service stubs.

## Frontend

You will need the [yarn package manager](https://yarnpkg.com) to run the UI. Install it using your package manager or:

```sh
curl -o- -L https://yarnpkg.com/install.sh | bash
```

Create a `ui/.env` file, using the `ui/.env.example`. Unless you changed the port of the backend, the default value will
be fine.

Then, you can start the frontend:

```sh
./frontend-start.sh
```

And open `http://localhost:8081`. The frontend will automatically reload when there are any changes in the frontend
source. The frontend connects to the backend on the 8080 port, as specified in the environment file.

# Project info

[The docs](http://softwaremill.github.io/bootzooka/) dive deeper into various aspects of the project (architecture, tech
stack, development tips).

## Commercial Support

We offer commercial support for Bootzooka and related technologies, as well as development services. [Contact
us](https://softwaremill.com) to learn more about our offer!

## Copyright

Copyright (C) 2013-2025 SoftwareMill [https://softwaremill.com](https://softwaremill.com).
