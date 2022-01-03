---
layout: default
title:  "Backend code structure"
---

## Backend code structure

The backend code is divided into a number of packages, each implementing a different functionality/use-case.

The classes in each package roughly follow a similar pattern:

* `XModule` trait. A module wires the object graph of classes in a single package (most often as `lazy val`s), using
dependencies from other packages (defined using `def`s). See [di-in-scala](http://di-in-scala.github.io/#modules) for
more details.
* `XModel` object. Contains [doobie](https://tpolecat.github.io/doobie/) queries to access the model corresponding
to the given functionality. The file also contains the model classes which are used in this functionality.
* `XService` class. Implements the business logic of the given functionality. Uses other services and models as
dependencies. May return results either as a `ConnectionIO[T]` object - describing operations on a database that can
be run in a single transaction with other operations. Or as a `IO[T]` - which describes a side-effecting,
asynchronous process. This can include communicating with external services, performing a whole database transaction
or processing data in parallel.
* `XApi` class. Defines descriptions of endpoints for the given functionality, as well as defines the server logic
for each endpoint. The server logic usually calls the corresponding service, or reads data from the model directly. All
of the classes used for input/output are defined in the companion object of the api class.

The `email`, `passwordreset`, `security` and `user` packages directly implement user-facing functionalities.

The `infrastructure` package contains utility classes for working with the database, JSON, logging and tracing.

The `util` package contains common classes, type aliases and extension methods which are used in other classes.
