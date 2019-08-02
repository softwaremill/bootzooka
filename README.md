# Bootzooka

[![Build Status](https://travis-ci.org/softwaremill/bootzooka.svg?branch=master)](https://travis-ci.org/softwaremill/bootzooka)

Bootzooka is simple project to quickly start developing a Scala-based microservice or web application

[See the docs](http://softwaremill.github.io/bootzooka/) for more information.

## Quick start

### Backend: PostgreSQL & API

In order to run Bootzooka, you'll need a running instance of the PostgreSQL with a `bootzooka` database. You can spin 
up one easily using docker:

```sh
# use "bootzooka" as a password
docker run --name bootzooka-postgres -p 5432:5432 -e POSTGRES_PASSWORD=bootzooka -e POSTGRES_DB=bootzooka -d postgres
```

Then, you can start the backend:

```sh
export SQL_PASSWORD=bootzooka
./backend-start.sh
```

### Frontend: Yarn & webapp

You will need the [yarn package manager](https://yarnpkg.com) to run the UI. Install it using your package manager or:

```sh
curl -o- -L https://yarnpkg.com/install.sh | bash
```

Then, you can start the frontend

```sh
./frontend-start.sh
```










