#!/bin/bash

DEBUG_SUSPEND=n
DEBUG_PORT=4888

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

echo "\$SCRIPT_DIR: ${SCRIPT_DIR}"

PROJECT_DIR=$( dirname $SCRIPT_DIR )

echo "\$PROJECT_DIR: ${PROJECT_DIR}"

cd $PROJECT_DIR

LUCEE_JAR=$(ls loader/target/lucee-*.jar 2>/dev/null)

if [[ -z "${LUCEE_JAR}" ]]; then
    echo "Building JAR"

    ant -f loader/build.xml fast

    if [[ $? -ne 0 ]]; then
        echo "Build failed"
        exit $?
    fi

    LUCEE_JAR=$(ls loader/target/lucee-*.jar 2>/dev/null)
else
    echo "Found existing Lucee JAR"
fi

if [[ -z "${LUCEE_JAR}" ]]; then
    echo "Lucee JAR does not exist"
    exit 1
fi

echo "Lucee JAR: ${LUCEE_JAR}"

export LUCEE_DEBUG_WEBXML=loader/src/main/resources/debug/web.xml

java -cp "${LUCEE_JAR}:dev/lib/*" \
    "-agentlib:jdwp=transport=dt_socket,server=y,suspend=${DEBUG_SUSPEND},address=${DEBUG_PORT}" \
    lucee.debug.Main
