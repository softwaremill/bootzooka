---
layout: default
title:  "Testing"
---

When you issue `test` from SBT, tests for both server-side and client-side components are run. SBT integrates some Grunt commands and executes tests for browser part via Grunt too.

Some of the backend tests use an embedded PostgreSQL instance. This instance is automatically unpacked, started before and stopped after the tests.
