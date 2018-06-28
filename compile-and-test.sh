#!/usr/bin/env bash
#
# script to start influxdb and compile influxdb-java with all tests.
#
set -e

DEFAULT_INFLUXDB_VERSION="1.5"
DEFAULT_MAVEN_JAVA_VERSION="3-jdk-10-slim"
DEFAULT_FLUX_DISABLE="true"

INFLUXDB_VERSION="${INFLUXDB_VERSION:-$DEFAULT_INFLUXDB_VERSION}"
MAVEN_JAVA_VERSION="${MAVEN_JAVA_VERSION:-$DEFAULT_MAVEN_JAVA_VERSION}"
FLUX_DISABLE="${FLUX_DISABLE:-$DEFAULT_FLUX_DISABLE}"

echo "Run tests with maven:${MAVEN_JAVA_VERSION} on onfluxdb-${INFLUXDB_VERSION} with flux disabled:${FLUX_DISABLE}"

docker kill influxdb || true
docker rm influxdb || true
docker pull influxdb:${version}-alpine || true
docker run \
          --detach \
          --name influxdb \
          --publish 8086:8086 \
          --publish 8089:8089/udp \
          --volume ${PWD}/influxdb.conf:/etc/influxdb/influxdb.conf \
      influxdb:${INFLUXDB_VERSION}-alpine

docker run -it --rm  \
      --volume $PWD:/usr/src/mymaven \
      --volume $PWD/.m2:/root/.m2 \
      --workdir /usr/src/mymaven \
      --link=influxdb \
      --env INFLUXDB_IP=influxdb \
        maven:${MAVEN_JAVA_VERSION} mvn clean install -DFLUX_DISABLE=${FLUX_DISABLE}

docker kill influxdb || true
