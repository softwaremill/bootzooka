#!/bin/sh

java -Xmx3000M -Xss1M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=1024m -jar sbt-launch.jar container:start "~ compile"
