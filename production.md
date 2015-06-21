---
layout: default
title:  "Production deployment"
---

## Fat jar

To build an executable jar, simply run (in sbt) `dist/assembly` (that is, the `assembly` task in the `dist`
subproject). This will create a fat-jar with all the code, processed javascript, css and html. You can run the jar
simply by running java:

    java -jar bootzooka-dist/target/scala-2.11/app.jar

## Deployable .war

To build a `.war`, run (in sbt) `backend/package`. The war will be located in `backend/target/scala-2.11/bootzooka.war`.
You can drop it in any servlet container (Tomcat/Jetty/JBoss/etc.)