#!/usr/bin/env bash
#
# script to start influxdb and compile influxdb-java with all tests.
#
set -e

DEFAULT_INFLUXDB_VERSION="1.6"
DEFAULT_MAVEN_JAVA_VERSION="3-jdk-10-slim"
DEFAULT_FLUX_DISABLE="false"

INFLUXDB_VERSION="${INFLUXDB_VERSION:-$DEFAULT_INFLUXDB_VERSION}"
MAVEN_JAVA_VERSION="${MAVEN_JAVA_VERSION:-$DEFAULT_MAVEN_JAVA_VERSION}"
FLUX_VERSION="nightly"
FLUX_DISABLE="${FLUX_DISABLE:-$DEFAULT_FLUX_DISABLE}"

echo "Run tests with maven:${MAVEN_JAVA_VERSION} on fluxdb-${INFLUXDB_VERSION} with flux disabled:${FLUX_DISABLE}"

docker kill influxdb || true
docker rm influxdb || true
docker pull influxdb:${INFLUXDB_VERSION}-alpine || true
docker run \
          --detach \
          --name influxdb \
          --publish 8086:8086 \
          --publish 8082:8082 \
          --publish 8089:8089/udp \
          --volume ${PWD}/influxdb.conf:/etc/influxdb/influxdb.conf \
      influxdb:${INFLUXDB_VERSION}-alpine

FLUX_CONTAINER_LINK=""
if [ ! "$FLUX_DISABLE" == "true" ]; then

    docker kill flux || true
    docker rm flux || true

    docker pull quay.io/influxdb/flux:${FLUX_VERSION}

    sleep 3
    docker run --detach --name flux --publish 8093:8093 quay.io/influxdb/flux:${FLUX_VERSION}

    FLUX_CONTAINER_LINK="--link=flux"
fi

test -t 1 && USE_TTY="-t"

docker run -i ${USE_TTY} --rm  \
      --volume $PWD:/usr/src/mymaven \
      --volume $PWD/.m2:/root/.m2 \
      --workdir /usr/src/mymaven \
      --link=influxdb \
      ${FLUX_CONTAINER_LINK} \
      --env INFLUXDB_IP=influxdb \
      --env FLUX_IP=flux \
        maven:${MAVEN_JAVA_VERSION} mvn clean install -rf :influxdb-flux -DFLUX_DISABLE=${FLUX_DISABLE}

docker kill influxdb || true

if [ ! "$FLUX_DISABLE" == "true" ]; then
    docker kill flux || true
fi
