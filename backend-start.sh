#!/bin/sh

docker 2>/dev/null
if [ $? -ne 0 ]; then
  echo "install docker first"
  exit 1
fi

docker ps -a | grep bootzookaMongo
if [ $? -ne 0 ]; then
  docker run --name=bootzookaMongo -d -p 27017:27017 mongo:latest
else
 docker start bootzookaMongo
fi

sbt container:start "~ compile"