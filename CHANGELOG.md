# Change Log
All notable changes to this project will be documented in this file.

## 2019-07-11
- rewrite of the backend using http4s, tapir, monix and doobie

## 2015-01-22
- MongoDB replaced by Slick & H2 with Flyway for easy database schema management

## 2014-12-16
- Adding mongo docker container start to backend-start.sh
- Dependency version update
- Adding changelog file
- explicit mongo-java-driver version (not inherited from lift's mongo record)
