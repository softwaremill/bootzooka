---
layout: default
title:  "Testing"
---

Because tests are using in-memory [H2 database](http://www.h2database.com/html/main.html) you don't need to have any database running on your machine.
Check out easy setup of in-memory database in `FlatSpecWithSql` trait.

When you issue `test` from SBT, tests for both server-side and client-side components are run. SBT integrates some Grunt
commands and executes tests for browser part via Grunt too.

## UI Tests

It is now also possible to run UI tests. We have added a new project, bootzooka-ui-tests, that contains tests for UI.
This project is not part of the normal build and hence these tests must be run manually. To do it, simply run sbt in the
bootzooka project directory, switch to bootzooka-ui-tests project and invoke the tests task.

    $ sbt
    [info] Loading project definition from /Users/pbu/Work/bootzooka/project/project
    [info] Loading project definition from /Users/pbu/Work/bootzooka/project
    [info] Compiling 1 Scala source to /Users/pbu/Work/bootzooka/project/target/scala-2.11/sbt-0.13/classes...
    [info] Set current project to bootzooka-root (in build file:/Users/pbu/Work/bootzooka/)
    > project bootzooka-ui-tests
    [info] Set current project to bootzooka-ui-tests (in build file:/Users/pbu/Work/bootzooka/)
    > test

Alternatively you can run a single test using the test-only task.

    > test-only uitest.ScalaRegisterUITest

These tests are written using WebDriver and __you need Firefox 20__ to properly run them.