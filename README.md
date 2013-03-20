# Bootzooka

Simple showcase project to allow quickly start with development process of modern web based application.

Live demo is available on http://bootzooka.softwaremill.com.
* [Changelog](#changelog)
    * [Iteration 10](#iteration-10)
    * [Iteration 9](#iteration-9)
    * [Iteration 8](#iteration-8)
    * [Iteration 7](#iteration-7)
    * [Iteration 6](#iteration-6)
* [Technology stack](#technology-stack)
    * [Why Scala?](#why-scala)
    * [Why AngularJS?](#why-angularjs)
    * [Why Scalatra?](#why-scalatra)
    * [Why Amazon SQS?](#why-amazon-sqs)
    * [Why sbt?](#why-sbt)
* [How to run](#how-to-run)
* [How to execute tests](#how-to-execute-tests)
* [How to develop](#how-to-develop)
    * [Useful sbt commands](#useful-sbt-commands)
    * [Keep code in shape](#keep-code-in-shape)
* [How to configure](#how-to-configure)   
* [License](#license)

Project is divided in few separated modules but basically we have UI which is base on [AngularJS](http://angularjs.org/)
and REST backend based on [Scalatra](http://www.scalatra.org/).
Backend can be interchanged as there is no dependency between frontend and backend - [spray.io](http://spray.io/) was also an option.


## Changelog

### Iteration 10
* Switch to Scala 2.10
* Salat DAOs migrated to Rogue
* Compatibility fixes for mobile browsers
* Reorganization of UI project structure

### Iteration 9
* UI tests reimplemented in Scala

### Iteration 8
* Possibility to run UI tests

### Iteration 7
* Changing profile data - login and e-mail
* Changing password

### Iteration 6
* Possibility to reset forgotten password

## Technology stack

### Why Scala?

Why not :-)

### Why AngularJS?

Basically it's the hottest JavaScirpt framework right now, developed and supported by Google.
It offers complete solution to build dynamic and modern HTML5 based web applications.
And at last from version 1.x is quite stable :-)

It's also worth to notice that there are plans to natively support DOM templating with the next generation of web browsers
- thus it can be huge advantage of the AngularJS over other frameworks.

### Why Scalatra?

It's quite simple and easy to jump into Scalatra for an ordinary Java developer whom used Servlets previously.
The syntax of the flow directives is straightforward and it was easy to integrate support for JSON into it.
And it's written in Scala from scratch which seamlessly integrates with other Scala based libraries.

### Why Amazon SQS?

It is a convenient queueing solution that allows to decuple message storing and reading. And it resides in the cloud, 
what could be more cool than that? Of course we could use simple queue stored in the MongoDB, but this project is
not supposed to be boring :)

### Why sbt?

The answer can be hard. It is easy to start using [sbt](http://www.scala-sbt.org/), but when things get hard, it's very difficult to find good examples
or supporting documents. But at the end it's a dedicated tool for Scala platform, so why not to try it :-)

## How to run

To run application, simply clone the source code, enter the directory and type _./run.sh_ or _run.bat_ depends on your OS,
navigate your web browser to http://localhost:8080/ and start using the application. By default application is using in-memory
queue and dummy e-mail sender.

## How to execute tests

Because some tests are using MongoDB you should have it installed on your machine. Additionally you should
let SBT know where MongoDB files are located. To do that please add one line to your ~/.sbt/local.sbt:

    SettingKey[File]("mongo-directory") := file("/Users/your_user/apps/mongodb")

with proper path to your MongoDB installation directory.

After that SBT will start MongoDB instance before executing each test class that exetends SpeficationWithMongo trait.

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

These tests are written using WebDriver and __you need Firefox 18__ to properly run them.

## How to develop

If you want to start develop new features, you must have sbt version 0.12.1 installed. Enter the same directory as above and type _sbt_
to start the sbt console. Few plugins are already integrated with t

* IDE configuration: we are using the best IDE right now - IntelliJ IDEA - to be able open project with it you must generate project files, you can do that with: _gen-idea_
* web server: right now Jetty is integrated with the project, you can start it from the sbt console with: _container:start_

There are two implementations of storage - in-memory and mongo - you must install MongoDB and start it before starting the application (when started with run.sh/run.bat the in-memory storage is used)

### Useful sbt commands

* _compile_ - compile the whole project
* _test_ - run all the tests
* _project &lt;sub-project-name&gt;_ - switch context to given sub-project, then all the commands will be execute only for that sub-project, thus can be also achieved with: _&lt;sub-project-name&gt;/test_
* _container:start_ - starts the embedded Jetty container
* _container:reload /_ - reloads application at context /
* _~;container:start; container:reload /_ - runs container and waits for source code changes to automatically compile changed file and to reload it
* _scalariform-format_ - execute Scalariform code formatter. More about it below in 'Keep code in shape' section
* _jslint_ - execute JSLint JavaScript code quality checker. It prints results to the console and writes them in /target/jslint/results.xml

### Keep code in shape

To keep code in shape we are using [Scalariform](https://github.com/mdr/scalariform) code formatter for Scala along with sbt plugin [Sbt-Scalariform](https://github.com/sbt/sbt-scalariform). We have intentionally disabled auto code-formatting during compile or test execution so to run formatter please use _sbt scalariform-format_ command. It checks code against various styling rules and applies all neccessary fixes. 

For JavaScript we are using [SBT JSLint Plugin](https://github.com/philcali/sbt-jslint) serving the same purpose. JSLint is executed during test phase.

## How to configure

All configuration should be stored in _application.conf_ file. Please check _application.conf.template_ to see what values are needed.

* To have Amazon SQS running you have to provide AWSAccessKeyId, SecretAccessKey and name of existing queue defined on your AWS account.
* To have e-mail sender working please provide smtp details (host, port, password, username). For smtp service working on localhost please comment smtpUsername key so EmailSender will know that he should use not secured smtp service.

_application.conf_ file should be placed next to _application.conf.template_

## License

Project is licensed under [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html) which means you can freely use any part of the project.
