# Bootzooka

Bootzooka is a simple application scaffolding project to allow quick start of development for modern web based
applications.

It contains only the very basic features, that almost any application needs (listed below). These features are fully
implemented both on server- and client- side (as a Single Page Application). The implementations can also serve
as blueprints for new functionalities.

Current user visible features:

*	User registration
* Lost password recovery (via e-mail)
*	Logging in/out
* Profile management

This may not sound "cool", but in fact Bootzooka is really helpful when bootstrapping a new project. This is because
besides of the features mentioned it contains the whole required setup and automation of build processes both for
frontend and backend. You get it out of the box which means significantly less time spent on setting up infrastructure
and tools and more time spent on actual coding features in project.

Live demo is available on [http://bootzooka.softwaremill.com](http://bootzooka.softwaremill.com).

## Technology stack

Bootzooka's stack consists of the following technologies/tools:

*	Scala (JVM based language)
*	Scalatra (simple web framework to expose JSON API)
*	H2 DB (persistence)
*	Slick (query SQL database using strictly typed DSL)
*   Flyway (easy schema evolution)
*	AngularJS + HTML5 (browser part)
*	SBT & Grunt.js (build tools)

### Why Scala?

A lot of the projects that we develop at [Softwaremill](http://softwaremill.com) are written in Scala. It's our
default go-to language for server-side. Softwaremill is also a [Typesafe](http://typesafe.com/) partner.

### Why AngularJS?

Basically it's the hottest JavaScript framework right now, developed and supported by Google and we use it a lot
in [SoftwareMill](http://softwaremill.com) (also because it's really good). It offers a complete solution to
build dynamic and modern HTML5 based web applications.

### Why Scalatra?

It's quite simple and easy to jump into Scalatra both for Java and Scala developers. The syntax of the flow directives
is straightforward and it was easy to integrate support for JSON into it. And it's written in Scala from scratch, hence
seamlessly integrates with other Scala based libraries.

### Why SBT and Grunt?

To put it simply, SBT is the build tool of choice for Scala, Grunt - for JavaScript.

## High-level architeture

Bootzooka is structured how modern web applications are done these days.

The backend server exposes a JSON API which can be consumed by any client you want. In case of Bootzooka this client
is a single-page browser browser application built with AngularJS. Such approach allows better scaling and independent
development the server and client parts. This separation is mirrored in how Bootzooka projects are structured.

There are several sub-projects (directories) containing server-side code and one for client-side application. They are
completely unrelated in terms of code and dependencies. `bootzooka-ui` directory contains the browser part (JavaScript,
CSS, HTML) and `bootzooka-backend` contains the backend application. Additionaly, `bootzooka-dist` is an utility module
for building a "fat jar" distribution.

## Prerequisities

In order to build and develop on Bootzooka foundations you need the following:

*	Java JDK >= 7
*	[SBT](http://www.scala-sbt.org/) >= 0.13
*	Node.js >= 0.10.13

## How to run (development)

Because (as said before) Bootzooka consists of two separate applications, in development you need to run both
separately.

**NOTE: This is not the case in production by default. When the final WAR package is built it contains both client and
server parts in one application that can be dropped into any servlet container.**

#### Server

To run the backend server part, enter the main directory and type `./backend-start.sh` or
`backend-start.bat` depending on your OS.

#### Browser client

To run the frontend server part, enter the main directory and type `./frontend-start.sh`. This should open
`http://0.0.0.0:9090/` in your browser (frontend listens on port 9090, backend on port 8080; so all HTTP requests
 will be proxied to port 8080).

For details of frontend build and architecture please refere to the [bootzooka-ui README](bootzooka-ui).

## How to run (production)

### Fat jar

To build an executable jar, simply run `bootzooka-dist/assembly` (that is, the `assembly` task in the `bootzooka-dist`
subproject). This will create a fat-jar with all the code, processed javascript, css and html. You can run the jar
simply by running java:

    java -jar bootzooka-dist/target/scala-2.11/bootzooka-dist-assembly-0.0.1-SNAPSHOT.jar

### Deployable .war

To build a `.war`, run `bootzooka-backend/package`. The war will be located in `bootzooka-backend/target/scala-2.11/bootzooka.war`.
You can drop it in any servlet container (Tomcat/Jetty/JBoss/etc.)

## How to execute tests

Because tests are using in-memory [H2 database](http://www.h2database.com/html/main.html) you don't need to have any database running on your machine.
Check out easy setup of in-memory database in `FlatSpecWithSQL` trait.

When you issue `test` from SBT, tests for both server-side and client-side components are run. SBT integrates some Grunt
commands and executes tests for browser part via Grunt too.

#### UI Tests

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

## Development tips

Generally during development you'll need two processes:

* sbt running the backend server (e.g. using `~;container:start; container:reload /`)
* grunt server which automatically picks up any changes

### IDE

We are using the best IDE right now: [IntelliJ IDEA](http://www.jetbrains.com/idea/). To generate an Idea project start
`sbt` and run `gen-idea`.

### Useful sbt commands

* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to the given sub-project, then all the commands will be execute only for
that sub-project, this can be also achieved with e.g.: `<sub-project-name>/test`
* `container:start` - starts the embedded Jetty container (backend)
* `~;container:start; container:reload /` - runs container (backend) and waits for source code changes to automatically
compile changed file and to reload it

### Database schema evolution

With Flyway, all you need to do is to put DDL script within bootzooka-backend/src/main/resources/db/migration/ directory.
You have to obey the following [naming convention](http://flywaydb.org/documentation/migration/sql.html): `V#__your_arbitrary_description.sql` where `#` stands for *unique* version of your schema.

## Configuration

Configuration is done through the [Typesafe Config](https://github.com/typesafehub/scalalogging) library.

Reference configuration is stored in the `reference.conf` file. You can either modify that file directly or override
it using system properties (see Typesafe Config's readme on how to do that).

* To have e-mail sender working please provide smtp details (host, port, password, username).

## Changelog

[changelog](CHANGELOG.md)

## License

Project is licensed under [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) which means you can
freely use any part of the project.
