# Bootzooka

Simple application scaffolding project to allow quick start of development process for modern web based application. It contains some features (that almost any application needs) out of the box (listed below). They are fully implemented on both server side and in browser client being modern Single Page Application.

*	Users registration & lost password recovery
*	Logging in/out & profile management

This may not sound really cool but in fact Bootzooka is really helpful when bootstrapping new project. This is because besides of features mentioned it contains all required setup and automation of build processes for both frontend and backend. You get it out of the box which means significantly less time spent on setting up infrastructure and tools and more time spent on actual coding features in project.

Live demo is available on [http://bootzooka.softwaremill.com](http://bootzooka.softwaremill.com).


## Technology stack

Bootzooka's stack consists of the following technologies/tools:

*	Scala (JVM based language)
*	Scalatra(simple web framework to expose JSON API)
*	MongoDB (persistence)
*	Rogue/Lift Record (talk to MongoDB from Scala)
*	AngularJS + HTML5 (browser part)
*	SBT & Grunt.js (build tools)

### Why Scala?

At [SoftwareMill](http://softwaremill.com) we have great number of our projects in Scala. It's our default go-to language for server-side.

### Why AngularJS?

Basically it's the hottest JavaScirpt framework right now, developed and supported by Google and we use it a lot in [SoftwareMill](http://softwaremill.com). It offers complete solution to build dynamic and modern HTML5 based web applications.

### Why Scalatra?

It's quite simple and easy to jump into Scalatra for an ordinary Java developer whom used Servlets previously. The syntax of the flow directives is straightforward and it was easy to integrate support for JSON into it. And it's written in Scala from scratch which seamlessly integrates with other Scala based libraries.

### Why SBT?

The answer can be hard. It is easy to start using [sbt](http://www.scala-sbt.org/), but when things get hard, it's very difficult to find good examples or supporting documents. But at the end it's a dedicated tool for Scala platform and it became de-facto standard in Scala community.

## High-level architeture

On the highest level Bootzooka is structured according to how modern web applications are done these days.

Server exposes JSON API which can be consumed by any client you want. In case of Bootzooka this client is typical browser application built with AngularJS. Such approach allows better scaling and independent development of both server and client parts. This separation is mirrored in how Bootzooka projects are structured.

There are several projects (directories) containing server-side code and one for client-side application. They are completely unrelated in terms of code and dependencies. `bootzooka-ui` directory contains browser part (JavaScript, CSS, HTML) and the others are building blocks of server application which contains its entry points in `bootzooka-rest` project.

## Prerequisities

In order to build and develop on Bootzooka foundations you need the following:

*	Java JDK
*	SBT (although it is also delivered as part of Bootzooka)
*	Node.js for frontend development automation

## How to run

Because (as said before) Bootzooka contains of two separate applications, in development you need to run both separately.

*** NOTE: This is not the case in production by default. When final WAR package is built it composes both client and server parts into one application that can be dropped into any servlet container. ***

#### Server
To run server part, enter the directory and type `./run.sh` or `run.bat` depends on your OS.

#### Browser client
To run Bootzooka in browser, go to `bootzooka-ui` project and issue `grunt server` from command line (if you have Grunt installed globally) or `node_modules/.bin/grunt server` if you have it locally.

For details of frontend build and architecture go to [bootzooka-ui project](bootzooka-ui).


## How to execute tests

Because some tests are using MongoDB you should have it installed on your machine. Additionally you should let SBT know where MongoDB files are located. To do that please add one line to your ~/.sbt/local.sbt:

    SettingKey[File]("mongo-directory") := file("/Users/your_user/apps/mongodb")

with proper path to your MongoDB installation directory.

After that SBT will start MongoDB instance before executing each test class that exetends SpeficationWithMongo trait.

When you issue `test` from SBT, tests for both server-side and client-side components are run. SBT integrates some Grunt commands and executes tests for browser part via Grunt too.

#### UI Tests

It is now also possible to run UI tests. We have added a new project, bootzooka-ui-tests, that contains tests for UI.
This project is not part of the normal build and hence these tests must be run manually. To do it, simply run sbt in the bootzooka
project directory, switch to bootzooka-ui-tests project and invoke the tests task.

    MacBook-Pro-Piotr:bootzooka pbu$ sbt
    [info] Loading project definition from /Users/pbu/Work/bootzooka/project/project
    [info] Loading project definition from /Users/pbu/Work/bootzooka/project
    [info] Compiling 1 Scala source to /Users/pbu/Work/bootzooka/project/target/scala-2.9.2/sbt-0.12/classes...
    [info] Set current project to bootzooka-root (in build file:/Users/pbu/Work/bootzooka/)
    > project bootzooka-ui-tests
    [info] Set current project to bootzooka-ui-tests (in build file:/Users/pbu/Work/bootzooka/)
    > test

Alternatively you can run a single test using the test-only task.

    > test-only uitest.ScalaRegisterUITest

These tests are written using WebDriver and __you need Firefox 20__ to properly run them.

## How to develop

### Server

If you want to start develop new features on server side, you must have sbt version 0.12.1 installed. Enter the same directory as above and type `sbt` to start the sbt console. Few plugins are already integrated with Bootzooka:

*	IDE configuration: we are using the best IDE right now - IntelliJ IDEA - to be able open project with it you must generate project files, you can do that with: `gen-idea`
* 	web server: right now Jetty is integrated with the project, you can start it from the sbt console with: `container:start`

There are two implementations of storage - in-memory and mongo - you must install MongoDB and start it before starting the application (when started with `run.sh`/`run.bat` the in-memory storage is used)

#### Useful sbt commands

* `compile` - compile the whole project
* `test` - run all the tests
* `project <sub-project-name>` - switch context to given sub-project, then all the commands will be execute only for that sub-project, thus can be also achieved with: `<sub-project-name>/test`
* `container:start` - starts the embedded Jetty container
* `container:reload /` - reloads application at context /
* `~;container:start; container:reload /` - runs container and waits for source code changes to automatically compile changed file and to reload it
* `scalariform-format` - execute Scalariform code formatter. More about it below in 'Keep code in shape' section

#### Keep code in shape

To keep code in shape we are using [Scalariform](https://github.com/mdr/scalariform) code formatter for Scala along with sbt plugin [Sbt-Scalariform](https://github.com/sbt/sbt-scalariform). We have intentionally disabled auto code-formatting during compile or test execution so to run formatter please use `sbt scalariform-format` command. It checks code against various styling rules and applies all neccessary fixes.

### Browser client

You need `node.js` and `grunt` to be available. For details please see [bootzooka-ui project](bootzooka-ui). It contains detailed instruction on how to set it up and run.

In general you just run `grunt server` (or `node_modules/.bin/grunt server` - details described in link above) which fires up your default browser and opens Bootzooka main page. This page consumes JSON API exposed by server and it all sums up to complete application. When you run grunt, files in `bootzooka-ui` project is watched and if any of them is changed Grunt does it's work and automatically reloads your browser with changes you've just made.

[bootzooka-ui project README](bootzooka-ui) contains detailed description of tasks available for Grunt such as tests, autotests, livereload, distribution building etc.

## How to configure

All configuration should be stored in _application.conf_ file. Please check _application.conf.template_ to see what values are needed.

* To have Amazon SQS running you have to provide AWSAccessKeyId, SecretAccessKey and name of existing queue defined on your AWS account.
* To have e-mail sender working please provide smtp details (host, port, password, username). For smtp service working on localhost please comment smtpUsername key so EmailSender will know that he should use not secured smtp service.

_application.conf_ file should be placed next to _application.conf.template_

## License

Project is licensed under [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) which means you can freely use any part of the project.