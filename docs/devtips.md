---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt, which auto-reloads the backend app on backend source code changes
* yarn server, which auto-reloads the frontend app on frontend source code changes

## Cloning

If you are planning to use Bootzooka as scaffolding for your own project, consider cloning the repo with `git clone
--depth 1` in order to start the history with last commit. You can now switch to your origin repository of choice with:
`git remote set-url origin https://repo.com/OTHERREPOSITORY.git`

## Useful sbt commands

* `renameProject` - replace Bootzooka with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be executed only
for that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `~backend/reStart` - runs the backend server and waits for source code changes to automatically compile changed file
  and to reload it. Used by the `./backend-start.sh` script

## Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/
directory. You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html):
`V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

## Developing frontend without backend

If you'd like to work only on the frontend, without starting the backend, you can proxy requests to a working, remote
backend instance. Update the `ui/.env` file accordingly.

## Imports

There are two imports that are useful when developing a new functionality:

### Database

If you are defining database queries or running transactions, add the following imports:

```scala
import com.softwaremill.bootzooka.infrastructure.Magnum.given
import com.augustnagro.magnum.{sql, DbTx}
```

This will bring into scope custom [Magnum](https://github.com/AugustNagro/magnum) codecs, the sql query interpolator
as well as the given instance which is required by methods that should run in a transaction.

### HTTP API

If you are defining new endpoints, import the base endpoints from `Http`, as well as Tapir:

```scala
import com.softwaremill.bootzooka.http.Http.*
import sttp.tapir.*
import sttp.tapir.json.jsoniter.*
```

This will bring into scope Tapir builder methods and schemas for documentation, along with Bootzooka-specific
customizations.

### Logging

Logging is performed using Slf4j. Extend `Logging` to bring into scope a `logger` value. The logs that are output to the
console include the current trace id.
