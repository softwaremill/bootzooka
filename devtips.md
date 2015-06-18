---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server (e.g. using `~;container:start; container:reload /`)
* grunt server which automatically picks up any changes

## Cloning

If you are planning to use Bootzooka as scaffolding for your own project, consider cloning the repo with `git clone --depth 1` in order to start the history with last commit.

## IDE

We are using the best IDE right now: [IntelliJ IDEA](http://www.jetbrains.com/idea/). To generate an Idea project start
`sbt` and run `gen-idea`.

## Useful sbt commands

* `renameProject` - replace Bootzooka with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be execute only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `container:start` - starts the embedded Jetty container (backend)
* `~;container:start; container:reload /` - runs container (backend) and waits for source code changes to automatically
compile changed file and to reload it

## Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/ directory.
You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.
