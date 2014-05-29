#!/bin/sh

# # # # # # # # # # # # # # # # # # # # # # # # #
# Script used to deploy project on demo server  #
# # # # # # # # # # # # # # # # # # # # # # # # #

JAVA_HOME="/opt/java/jdk1.7.0_09"
export JAVA_HOME

# shutdown jetty
jetty/bin/jetty.sh stop

# clear logs
rm jetty/logs/*

# update source code
cd bootzooka
git pull

# wait 5 sec
sleep 5

# copy configuration file to proper location
cp /home/bootstrap/bootzooka/application.conf /home/bootstrap/bootzooka/bootzooka/bootzooka-rest/src/main/resources/

# package
sbt compile package

# deploy
cp bootzooka-ui/target/scala-2.10/bootzooka.war ../jetty/webapps/

# start jetty
cd ../
jetty/bin/jetty.sh start