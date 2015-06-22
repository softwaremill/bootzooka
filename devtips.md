---
layout: default
title:  "Development tips"
---

Generally during development you'll need two processes:

* sbt running the backend server (e.g. using `~;container:start; container:reload /`)
* grunt server which automatically picks up any changes

## Cloning

If you are planning to use Bootzooka as scaffolding for your own project, consider cloning the repo with `git clone --depth 1` in order to start the history with last commit. You can now switch to your origin repository of choice with: `git remote set-url origin https://repo.com/OTHERREPOSITORY.git`  

## IDE

You can use either [IntelliJ IDEA](http://www.jetbrains.com/idea/) or [Scala Eclipse IDE](http://scala-ide.org). 

## Useful sbt commands

* `renameProject` - replace Bootzooka with your custom name and adjust scala package names
* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be executed only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `container:start` - starts the embedded Jetty container (backend)
* `~;container:start; container:reload /` - runs container (backend) and waits for source code changes to automatically
compile changed file and to reload it

## Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/ directory.
You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

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