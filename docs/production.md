---
layout: default
title:  "Production deployment"
---

## Fat jar

To build an executable jar, simply run (in sbt) `backend/assembly` (that is, the `assembly` task in the `backend` subproject). This will create a fat-jar with all the code, processed javascript, css and html. You can run the jar simply by running java:

```
java -jar backend/target/scala-VERSION/bootzooka.jar
```

## Docker

To build a docker image, run `backend/docker:publishLocal`. This will create the `docker:latest` image.

You can test the image by using the provided `docker-compose.yml` file.

## Kubernetes

Use [Helm](https://helm.sh/) to easily deploy Bootzooka into [Kubernetes](https://kubernetes.io/) cluster.

### Add SoftwareMill Helm repository

```
helm repo add softwaremill https://charts.softwaremill.com/
helm repo update
```

### Fetch and Customize Bootzooka chart

```
helm fetch softwaremill/bootzooka --untar
```

### Install Bootzooka chart

```
helm install --generate-name bootzooka
```

Please see [Bootzooka Helm Chart documentation](https://github.com/softwaremill/bootzooka/blob/master/helm/bootzooka/README.md) for more information, including configuration options.
