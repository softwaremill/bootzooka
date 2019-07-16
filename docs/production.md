---
layout: default
title:  "Production deployment"
---

## Fat jar

To build an executable jar, simply run (in sbt) `backend/assembly` (that is, the `assembly` task in the `backend` subproject). This will create a fat-jar with all the code, processed javascript, css and html. You can run the jar simply by running java:

```
java -jar backend/target/scala-2.12/bootzooka.jar
```

## Docker

To build a docker image, run `backend/docker:publishLocal`. This will create the `docker:latest` image.
