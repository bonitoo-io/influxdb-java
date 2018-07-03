#!/usr/bin/env bash
#
# script to start influxdb and compile influxdb-java with all tests.
#
set -e

DEFAULT_INFLUXDB_VERSION="1.5"
DEFAULT_MAVEN_JAVA_VERSION="3-jdk-10-slim"
DEFAULT_FLUX_DISABLE="false"

INFLUXDB_VERSION="${INFLUXDB_VERSION:-$DEFAULT_INFLUXDB_VERSION}"
MAVEN_JAVA_VERSION="${MAVEN_JAVA_VERSION:-$DEFAULT_MAVEN_JAVA_VERSION}"
FLUX_DISABLE="${FLUX_DISABLE:-$DEFAULT_FLUX_DISABLE}"

echo "Run tests with maven:${MAVEN_JAVA_VERSION} on fluxdb-${INFLUXDB_VERSION} with flux disabled:${FLUX_DISABLE}"

docker kill influxdb || true
docker rm influxdb || true
docker pull influxdb:${version}-alpine || true
docker run \
          --detach \
          --name influxdb \
          --publish 8086:8086 \
          --publish 8082:8082 \
          --publish 8089:8089/udp \
          --volume ${PWD}/influxdb.conf:/etc/influxdb/influxdb.conf \
      influxdb:${INFLUXDB_VERSION}-alpine

if [ ! "$FLUX_DISABLE" == "true" ]; then

    killall fluxd || true
    
    # OS Type
    case "$OSTYPE" in
      darwin*)
        archive='platform_nightly_macOS_amd64.tar.gz'
         ;;
      linux*)
        archive="platform_nightly_linux_amd64.tar.gz" ;;
      msys*)
        archive="platform_nightly_windows_amd64.zip" ;;
      *)
        echo "unknown: $OSTYPE";
        exit 1 ;;
    esac

    # Download/extract the new nightly build
    baseUrl="https://dl.influxdata.com/flux/nightlies/"
    if [[ $(find "${archive}" -mtime +1 -print) ]] || [ ! -e "$archive" ] ; then
      rm -rf platform_nightly*
      wget --no-use-server-timestamps ${baseUrl}${archive} -O ${archive}
      tar xvfz ${archive}
    fi

    FLUX_IP=`ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p' | head -n 1`
    echo "FLUX_IP:" ${FLUX_IP}
    sleep 3
    platform_nightly_*/fluxd &
fi

test -t 1 && USE_TTY="-t"

docker run -i ${USE_TTY} --rm  \
      --volume $PWD:/usr/src/mymaven \
      --volume $PWD/.m2:/root/.m2 \
      --workdir /usr/src/mymaven \
      --link=influxdb \
      --env INFLUXDB_IP=influxdb \
      --env FLUX_IP=${FLUX_IP} \
        maven:${MAVEN_JAVA_VERSION} mvn clean install -DFLUX_DISABLE=${FLUX_DISABLE}

docker kill influxdb || true

if [ ! "$FLUX_DISABLE" == "true" ]; then
    killall fluxd || true
fi
