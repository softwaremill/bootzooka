#!/bin/bash

echo "This script might be run with mongo in docker container with: --docker"
while [[ $1 = -* ]]; do
    arg=$1; shift

    case $arg in
        --docker)
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
            ;;
    esac
done

sbt container:start "~ compile"