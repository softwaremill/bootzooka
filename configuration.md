---
layout: default
title:  "Configuration"
---

Configuration is done through the [Typesafe Config](https://github.com/typesafehub/scalalogging) library.

Reference configuration is stored in the `reference.conf` file. You can either modify that file directly or override
it using system properties (see Typesafe Config's readme on how to do that).

* To have e-mail sender working please provide smtp details (host, port, password, username).
