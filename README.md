# Bootzooka

[![Build Status](https://travis-ci.org/softwaremill/bootzooka.svg?branch=master)](https://travis-ci.org/softwaremill/bootzooka)

Bootzooka is a simple application scaffolding project to allow quick start of development for modern web based
applications.

[See the docs](http://softwaremill.github.io/bootzooka/) for more information.

## Requirements

### PostgreSQL
In order to run Bootzooka, you need a running instance of the PostgreSQL with a `postgres` database. You can spin up one easily using docker:
```sh
# use "bootzooka" as a password
docker run --name bootzooka-postgres -p 5432:5432 -e POSTGRES_PASSWORD=bootzooka -e POSTGRES_DB=bootzooka -d postgres

export SQL_PASSWORD=bootzooka

./backend-start.sh
```

### Yarn
You will need a [Yarn package manager](https://yarnpkg.com) to run the UI, eg:
```sh
curl -o- -L https://yarnpkg.com/install.sh | bash

./frontend-start.sh
```










