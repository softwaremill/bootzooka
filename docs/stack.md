---
layout: default
title:  "Technology stack"
---

When picking Bootzooka's technology stack we wanted to use modern, but reasonably proven technologies. So while you won't find the latest, hottest frameworks here, you also won't see any JSPs or &lt;marquee&gt; tags. The components are easy to replace, so if you'd like to experiment with a new library, this should be a matter of replacing only a small part of Bootzooka. Also, we try to update the stack once in a while, so that it's up-to-date with current developments and trends.

Bootzooka's stack consists of the following technologies/tools, on the backend:

* [Scala](https://www.scala-lang.org) (JVM based, functional language)
* [Tapir](https://github.com/softwaremill/tapir) (endpoint description library) + [netty](https://netty.io) (backend networking layer)
* SQL database, by default [PostgreSQL](https://www.postgresql.org) (persistence)
* [Magnum](https://github.com/AugustNagro/magnum) (type-safe database access) + [flyway](https://flywaydb.org) (schema evolution)
* [Ox](https://github.com/softwaremill/ox) (error handling, concurrency & resource management)
* [SBT](https://www.scala-sbt.org) (build tool)
* [OpenTelemetry](https://opentelemetry.io) (metrics & tracing)

And on the frontend:

* [TypeScript](https://www.typescriptlang.org) (JavaScript superset)
* [react](https://reactjs.org)
* [Swagger](https://swagger.io) (interactive API docs)
* [yarn](https://yarnpkg.com) (build tool)
* [openapi-client-axios](https://www.npmjs.com/package/openapi-client-axios) (JS library for consuming OpenAPI-enabled APIs)
* [formik](https://formik.org/) (forms)
* [yup](https://www.npmjs.com/package/yup/v/1.3.3) (validation)

### Why Scala?

Scala is a strongly-typed functional programming language, running (among others) on the JVM. It offers a range of tools to define flexible abstractions, increasing the quality and safety of the code being written, while keeping the codebase concise and free from boilerplate. For a deeper dive, see the ["Why Scala"](https://blog.softwaremill.com/why-scala-a6ac8c98c541) and ["Principles of developing applications in Scala"](https://softwaremill.com/principles-of-developing-applications-in-scala/) articles.

A lot of the projects that we develop at [SoftwareMill](http://softwaremill.com) are written in Scala. It's our go-to language for non-trivial server-side services.

### Why React?

React is one of the most popular JavaScript framework right now, developed and supported by Facebook; we use it frequently at [SoftwareMill](http://softwaremill.com) (also because it's really good). It offers a complete solution to build dynamic and modern HTML based web applications, with a "functional" approach.

### Why Ox?

Ox provides the necessary utilities needed when writing direct-style Scala: structured concurrency, error handling, resource management and resiliency.

### Why tapir + netty?

Tapir defines a programmer-friendly API for describing HTTP endpoints which can be interpreted as a server, or as Swagger/OpenAPI documentation. Netty is a battle-proven and fast networking library for the JVM.

### Why SBT and Yarn?

To put it simply, [SBT](https://www.scala-sbt.org) is the build tool of choice for Scala, yarn - for JavaScript.

### Why openapi-client-axios?

It's a simple and easy-to-use JS library that generates API clients based on the OpenAPI specification with axios. It simplifies client-side development, comparing to OpenAPI which is more versatile and foundational but requires additional tooling to provide similar functionality.
