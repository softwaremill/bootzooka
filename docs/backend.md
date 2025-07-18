---
layout: default
title:  "Backend code structure"
---

## Backend code structure

The backend code is divided into a number of packages, each implementing a different functionality/use-case.

The classes in each package follow a similar pattern:

* `XModel` class. Contains queries to access the model corresponding to the given functionality. The file also contains
the model classes which are used in this functionality.
* `XService` class. Implements the business logic of the given functionality. Uses other services and models as
dependencies. May return results that either need to be run within a transaction (when there's a `using DbTx` parameter
list), or returns result directly. Results might be wrapped in `Either[Fail, ...]`, when "expected" errors might occur
(e.g. validation errors). A service implementation might include communicating with external services or performing
whole database transactions.
* `XApi` class. Defines descriptions of endpoints for the given functionality, as well as defines the server logic for
each endpoint. The server logic usually calls the corresponding service, or reads data from the model directly. All
classes used for input/output are defined in the companion object of the api class.

The `email`, `passwordreset`, `security` and `user` packages directly implement user-facing functionalities.

The `infrastructure` and `logging` packages contain utility classes for working with the database, logging and tracing.

The `util` package contains common classes, type aliases and extension methods which are used in other classes.
