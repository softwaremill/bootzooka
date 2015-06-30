---
layout: default
title:  "Technology stack"
---

When picking Bootzooka's technology stack we wanted to use modern, but reasonably proven technologies. So while you won't find the latest, hottest frameworks here, you also won't see any JSPs or &lt;marquee&gt; tags. The components are easy to replace, so if you'd like to experiment with a new library, this should be a matter of replacing only a small part of Bootzooka. Also, we try to update the stack once in a while, so that it's up-to-date with current developments and trends.

Bootzooka's stack consists of the following technologies/tools:

*	Scala (JVM based language)
*	Scalatra (simple web framework to expose JSON API)
*	SQL database, by default H2 DB (persistence)
*	Slick (query SQL database using strictly typed DSL)
*   Flyway (easy schema evolution)
*	AngularJS + HTML5 (frontend)
*	SBT & Grunt.js (build tools)
*   Swagger (interactive API docs)

### Why Scala?

A lot of the projects that we develop at [Softwaremill](http://softwaremill.com) are written in Scala. It's our default go-to language for server-side. Softwaremill is also a [Typesafe](http://typesafe.com/) partner.

### Why AngularJS?

Angular the most popular JavaScript framework right now, developed and supported by Google and we use it frequently at [SoftwareMill](http://softwaremill.com) (also because it's really good). It offers a complete solution to build dynamic and modern HTML5 based web applications.

### Why Scalatra?

It's quite simple and easy to jump into Scalatra both for Java and Scala developers. The syntax of the flow directives is straightforward and it was easy to integrate support for JSON into it. And it's written in Scala from scratch, hence seamlessly integrates with other Scala based libraries.

### Why SBT and Grunt?

To put it simply, SBT is the build tool of choice for Scala, Grunt - for JavaScript.