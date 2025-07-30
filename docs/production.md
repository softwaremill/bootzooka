---
layout: default
title:  "Production deployment"
---

## Docker

To build a docker image, run `docker/Docker/publishLocal`. This will create the `docker:latest` image.

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

Please see [Bootzooka Helm Chart
documentation](https://github.com/softwaremill/bootzooka/blob/master/helm/bootzooka/README.md) for more information,
including configuration options.
