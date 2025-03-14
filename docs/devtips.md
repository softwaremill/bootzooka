---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server
* yarn server which automatically picks up any changes

## Cloning

If you are planning to use Bootzooka as scaffolding for your own project, consider cloning the repo with `git clone --depth 1` in order to start the history with last commit. You can now switch to your origin repository of choice with: `git remote set-url origin https://repo.com/OTHERREPOSITORY.git`

## Useful sbt commands

* `renameProject` - replace Bootzooka with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be executed only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `~backend/re-start` - runs the backend server and waits for source code changes to automatically compile changed file and to reload it

## Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/ directory. You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

## Developing frontend without backend

If you'd like to work only on the frontend, without starting the backend, you can proxy requests to a working, remote backend instance. In `ui/package.json` you need to edit the proxy settings.

## Imports

There are two imports that are useful when developing a new functionality:

### Database

If you are defining database queries or running transactions, add the following import:

```scala
import com.softwaremill.bootzooka.infrastructure.Magnum.*
```

This will bring into scope custom [Magnum](https://github.com/AugustNagro/magnum) codecs.

### HTTP API

If you are describing new endpoints, import all members of `Http`:

```scala
import com.softwaremill.bootzooka.http.Http.*
```

This will bring into scope Tapir builder methods and schemas for documentation, along with Bootzooka-specific customizations.

### Logging

Logging is performed using Slf4j. Extend `Logging` to bring into scope a `logger` value. The logs that are output to the console include the current trace id.
