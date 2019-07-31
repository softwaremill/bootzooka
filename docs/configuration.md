---
layout: default
title:  "Configuration"
---

Configuration uses the [Typesafe Config](https://github.com/lightbend/config) file format (HOCON), but is read using [pureconfig](https://pureconfig.github.io) library.

The configuration is stored in the `application.conf` file. You can either modify that file directly or override it using system properties (see Typesafe Config's readme on how to do that).

* To have e-mail sender working please provide smtp details (host, port, password, username).

## Project name customization

If you want to use Bootzooka as a scaffolding for your own project, use the `renameProject` command with sbt, for example:  

````
sbt "renameProject com.mycompany foobar"
````  

This should rename your project to **Foobar**, move all sources to top-level package `com.mycompany.foobar`.
