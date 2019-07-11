---
layout: default
title:  "Technology stack"
---

When picking Bootzooka's technology stack we wanted to use modern, but reasonably proven technologies. So while you won't find the latest, hottest frameworks here, you also won't see any JSPs or &lt;marquee&gt; tags. The components are easy to replace, so if you'd like to experiment with a new library, this should be a matter of replacing only a small part of Bootzooka. Also, we try to update the stack once in a while, so that it's up-to-date with current developments and trends.

Bootzooka's stack consists of the following technologies/tools:

*	[scala](https://www.scala-lang.org) (JVM based language)
*	[http4s](https://http4s.org) (lightweight HTTP library) + tapir (endpoint description library)
*	SQL database, by default [PostgreSQL](https://www.postgresql.org) (persistence)
*	[doobie](https://tpolecat.github.io/doobie/) (query SQL database using strictly typed DSL)
*   [flyway](https://flywaydb.org) (easy schema evolution)
* [monix](https://monix.io) (managing side-effects and concurrency in the backend)
*	AngularJS + HTML5 (frontend)
*	[SBT](https://www.scala-sbt.org) & Webpack (build tools)
*   Swagger (interactive API docs)

### Why Scala?

A lot of the projects that we develop at [SoftwareMill](http://softwaremill.com) are written in Scala. It's our default go-to language for server-side. SoftwareMill is also a [Lightbend](http://lightbend.com/) partner.

### Why AngularJS?

Angular the most popular JavaScript framework right now, developed and supported by Google and we use it frequently at [SoftwareMill](http://softwaremill.com) (also because it's really good). It offers a complete solution to build dynamic and modern HTML5 based web applications.

### Why http4s + tapir?

http4s is a popular ligthweight, functional library for exposing HTTP servers. tapir on the other hand, defines a programmer-friendly API for describing HTTP endpoints which can be interpreted as a server (using http4s as the backing implementation), or as Swagger/OpenAPI documentation.

### Why SBT and Webpack?

To put it simply, [SBT](https://www.scala-sbt.org) is the build tool of choice for Scala, Webpack - for JavaScript.
