---
layout: default
title:  "Technology stack"
---

When picking Bootzooka's technology stack we wanted to use modern, but reasonably proven technologies. So while you won't find the latest, hottest frameworks here, you also won't see any JSPs or &lt;marquee&gt; tags. The components are easy to replace, so if you'd like to experiment with a new library, this should be a matter of replacing only a small part of Bootzooka. Also, we try to update the stack once in a while, so that it's up-to-date with current developments and trends.

Bootzooka's stack consists of the following technologies/tools:

*	Scala (JVM based language)
*	Akka HTTP (lightweight HTTP library with elegant routing DSL)
*	SQL database, by default H2 DB (persistence)
*	Slick (query SQL database using strictly typed DSL)
*   Flyway (easy schema evolution)
*	AngularJS + HTML5 (frontend)
*	SBT & Webpack (build tools)
*   Swagger (interactive API docs)

### Why Scala?

A lot of the projects that we develop at [Softwaremill](http://softwaremill.com) are written in Scala. It's our default go-to language for server-side. Softwaremill is also a [Lightbend](http://lightbend.com/) (former Typesafe) partner.

### Why AngularJS?

Angular the most popular JavaScript framework right now, developed and supported by Google and we use it frequently at [SoftwareMill](http://softwaremill.com) (also because it's really good). It offers a complete solution to build dynamic and modern HTML5 based web applications.

### Why Akka HTTP?

Akka HTTP is the successor to the popular [Spray.io library](http://spray.io), offers a lightweight HTTP server as well as an elegant, easy-to-learn, flexible DSL for defining routes. Moreover, Akka HTTP is a library, not a framework, so it leaves a lot of freedom when choosing other components.

### Why SBT and Webpack?

To put it simply, SBT is the build tool of choice for Scala, Webpack - for JavaScript.