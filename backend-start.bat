@setlocal

REM by default, reporting metrics is disabled
set OTEL_SDK_DISABLED=true

REM Bootzooka uses the OTLP/HTTP protocol to export metrics (see the observabilityDependencies in build.sbt)
set OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf

sbt "~backend/reStart"
