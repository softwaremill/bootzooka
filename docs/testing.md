---
layout: default
title:  "Testing"
---

When you issue `test` from SBT, tests for both server-side and client-side components are run. SBT integrates some Grunt commands and executes tests for browser part via Yarn too.

Some of the backend tests use an embedded PostgreSQL instance. This instance is automatically downloaded (via Docker), started before and stopped after the tests.

In case of integration tests, there is subproject - “integration-tests”. It contains composed environment to launch application with PostgreSQL db and [mailhog](https://github.com/mailhog/MailHog) as SMTP server. For more details, please look at proper [docker-compose.yml](https://github.com/softwaremill/bootzooka/tree/master/integration-tests/docker-compose.yml)
