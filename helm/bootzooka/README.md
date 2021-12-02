# bootzooka

![Version: 0.2.1](https://img.shields.io/badge/Version-0.2.1-informational?style=flat-square) ![Type: application](https://img.shields.io/badge/Type-application-informational?style=flat-square) ![AppVersion: 1.0](https://img.shields.io/badge/AppVersion-1.0-informational?style=flat-square)

A Helm chart for Bootzooka

**Homepage:** <https://softwaremill.github.io/bootzooka/>

## Installation

### Add Helm repository

```
helm repo add softwaremill https://charts.softwaremill.com/
helm repo update
```

## Fetch and Customize Bootzooka chart
```
helm fetch softwaremill/bootzooka --untar
```

## Install Bootzooka chart

```
helm install --generate-name bootzooka
```

## Configuration

The following table lists the configurable parameters of the chart and the default values.

## Values

| Key | Type | Default | Description |
|-----|------|---------|-------------|
| bootzooka.affinity | object | `{}` |  |
| bootzooka.fullnameOverride | string | `""` |  |
| bootzooka.image.pullPolicy | string | `"Always"` |  |
| bootzooka.image.repository | string | `"softwaremill/bootzooka"` |  |
| bootzooka.image.tag | string | `"latest"` |  |
| bootzooka.ingress.annotations."kubernetes.io/ingress.class" | string | `"nginx"` |  |
| bootzooka.ingress.annotations."kubernetes.io/tls-acme" | string | `"true"` |  |
| bootzooka.ingress.enabled | bool | `true` |  |
| bootzooka.ingress.hosts[0].host.domain | string | `"bootzooka.example.com"` |  |
| bootzooka.ingress.hosts[0].host.path | string | `"/"` |  |
| bootzooka.ingress.hosts[0].host.pathType | string | `"ImplementationSpecific"` |  |
| bootzooka.ingress.hosts[0].host.port | string | `"http"` |  |
| bootzooka.ingress.tls[0].hosts[0] | string | `"bootzooka.example.com"` |  |
| bootzooka.ingress.tls[0].secretName | string | `"bootzooka-tls"` |  |
| bootzooka.ingress.tls_enabled | bool | `false` |  |
| bootzooka.nameOverride | string | `""` |  |
| bootzooka.nodeSelector | object | `{}` |  |
| bootzooka.replicaCount | int | `1` |  |
| bootzooka.reset_password_url | string | `"https://bootzooka.example.com/password-reset?code=%s"` |  |
| bootzooka.resources | object | `{}` |  |
| bootzooka.service.port | int | `8080` |  |
| bootzooka.service.type | string | `"ClusterIP"` |  |
| bootzooka.smtp.enabled | bool | `true` |  |
| bootzooka.smtp.from | string | `"hello@bootzooka.example.com"` |  |
| bootzooka.smtp.host | string | `"server.example.com"` |  |
| bootzooka.smtp.password | string | `"bootzooka"` |  |
| bootzooka.smtp.port | int | `465` |  |
| bootzooka.smtp.ssl | string | `"true"` |  |
| bootzooka.smtp.ssl_ver | string | `"false"` |  |
| bootzooka.smtp.username | string | `"server.example.com"` |  |
| bootzooka.sql.host | string | `"{{ .Values.postgresql.fullnameOverride }}"` | Value will be taken from 'postgresql.fullnameOverride' setting |
| bootzooka.sql.name | string | `"{{ .Values.postgresql.postgresqlDatabase }}"` | Value will be taken from 'postgresql.postgresqlDatabase' setting |
| bootzooka.sql.password | string | `"{{ .Values.postgresql.postgresqlPassword }}"` | Value will be taken from 'postgresql.postgresqlPassword' setting |
| bootzooka.sql.port | string | `"{{ .Values.postgresql.service.port }}"` | Value will be taken from 'postgresql.service.port' setting |
| bootzooka.sql.username | string | `"{{ .Values.postgresql.postgresqlUsername }}"` | Value will be taken from 'postgresql.postgresqlUsername' setting |
| bootzooka.tolerations | list | `[]` |  |
| postgresql.connectionTest.image.pullPolicy | string | `"IfNotPresent"` |  |
| postgresql.connectionTest.image.repository | string | `"bitnami/postgresql"` |  |
| postgresql.connectionTest.image.tag | int | `11` |  |
| postgresql.enabled | bool | `true` | Disable if you already have PostgreSQL running in cluster where Bootzooka chart is being deployed |
| postgresql.fullnameOverride | string | `"bootzooka-pgsql-postgresql"` |  |
| postgresql.postgresqlDatabase | string | `"bootzooka"` |  |
| postgresql.postgresqlPassword | string | `"bootzooka"` |  |
| postgresql.postgresqlUsername | string | `"postgres"` |  |
| postgresql.service.port | int | `5432` |  |
