#!/bin/bash

# by default, reporting metrics is disabled
export OTEL_SDK_DISABLED=true

# Bootzooka uses the OTLP/HTTP protocol to export metrics (see the observabilityDependencies in build.sbt)
export OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf

export OTEL_SERVICE_NAME=bootzooka

# --server runs sbt in the foreground
sbt --server "~backend/reStart"
