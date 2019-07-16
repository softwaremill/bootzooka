#!/usr/bin/env bash

set -x

CGROUP_MEMORY_LIMIT_FILE="/sys/fs/cgroup/memory/memory.limit_in_bytes"
if [ -f $CGROUP_MEMORY_LIMIT_FILE ]; then
    MAXRAM=$(cat ${CGROUP_MEMORY_LIMIT_FILE})
else
    echo "This script is designed to run inside docker only, exiting..."
    exit 1
fi

TOTAL_MEMORY=$(($(cat /proc/meminfo  |head -n 1 |awk '{print $2}')*1024))

if [ "${MAXRAM}" -lt "${TOTAL_MEMORY}" ]; then
    XMXPERCENT="${XMXPERCENT:-80}"
    XMX=$(($MAXRAM-$MAXRAM/100*(100-$XMXPERCENT)))
    XMX_CONFIG="-J-Xmx${XMX} -J-Xms${XMX}"
fi

LOG_LEVEL="${LOG_LEVEL:-INFO}"

[ "${LOG_HOST}" -a "${LOG_PORT}" ] && LOGGING_CONFIG="-DLOG_HOST=${LOG_HOST} -DLOG_PORT=${LOG_PORT} -DAPP_ENV=${APP_ENV} -DAPP_NAME=${APP_NAME} -DSERVICE_INSTANCE_ID=${SERVICE_INSTANCE_ID} -DHOST_ID=${HOST_ID} -DLOG_LEVEL=${LOG_LEVEL}"

echo "Starting the JVM process"
command="$@"
exec $command ${LOGGING_CONFIG} ${XMX_CONFIG}
