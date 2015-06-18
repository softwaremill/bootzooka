---
layout: default
title:  "Configuration"
---

Configuration is done through the [Typesafe Config](https://github.com/typesafehub/scalalogging) library.

Reference configuration is stored in the `reference.conf` file. You can either modify that file directly or override
it using system properties (see Typesafe Config's readme on how to do that).

* To have e-mail sender working please provide smtp details (host, port, password, username).

## Project name customization
If you want to use Bootzooka as a scaffolding for your own project, use the `renameProject` command with sbt, for example:  
````
sbt renameProject com.mycompany foobar
````  
This should rename your project to **Foobar**, move all sources to top-level package `com.mycompany.foobar`.
