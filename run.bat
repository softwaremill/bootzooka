@setlocal

java -DwithInMemory=true -jar sbt-launch.jar container:start "~ compile"