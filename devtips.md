---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server 
* grunt server which automatically picks up any changes

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

If you'd like to work only on the frontend, without starting the backend, you can proxy requests to a working, remote backend instance. In `ui/Gruntfile.js` you need to edit the proxy settings and change the `proxies` option in `connect` to point to the backend instance, e.g.:

```
proxies: [ {
    context: '/rest/',
    host: 'my-backend.com',
    port: 80,
    headers: {
        'host': 'my-backend.com'
    }
} ]
```

## JSON

For serializing data to JSON the [Circe](https://github.com/travisbrown/circe) library is used. It relies on compile-time codec generation, instead of run-time reflection. If in your endpoint, you want to send a response to the client which corresponds to a case class, you need to:

1. make sure that the content of `JsonSupport` is in scope (e.g. by extending the trait itself or the more general `RoutesSupport`)
2. `import io.circe.generic.auto._` which will automatically generate a codec from the case class at compile-time
3. define an implicit `CanBeSerialized[T]` instance for the type `T` that you want to send. This is a feature of Bootzooka, not normally required, but included to make sure that you only send data that indeed should be sent (to avoid automatically serializing e.g. a list of `User` instances which contains the password hashes)

Of course, the existing endpoints (for managing users, getting the version) have all of that ready.
