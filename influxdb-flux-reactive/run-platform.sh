#!/usr/bin/env bash

baseUrl="https://dl.influxdata.com/flux/nightlies/"
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


#### Download/extract the new nightly build
if [[ $(find "${archive}" -mtime +100 -print) ]] || [ ! -e "$archive" ] ; then
  rm -rf platform_nightly*
  wget ${baseUrl}${archive} -O ${archive}
  tar xvfz ${archive}
fi

docker network create flux-net

docker stop flux-influxdb
docker rm flux-influxdb
docker run -d -p 8086:8086 -p 8082:8082 -v $PWD/conf/influxdb.toml:/etc/influxdb/influxdb.conf:ro \
    -v influxdb:/var/lib/influxdb --net=flux-net  --name flux-influxdb influxdb:latest -config /etc/influxdb/influxdb.conf

docker stop flux-chronograf
docker rm flux-chronograf
docker run -d -p 8888:8888 --net=flux-net --name flux-chronograf chronograf:latest --influxdb-url=http://flux-influxdb:8086

docker stop flux-telegraf
docker rm flux-telegraf
docker run -d --net flux-net -v $PWD/conf/telegraf.toml:/etc/telegraf/telegraf.conf:ro --name flux-telegraf telegraf:latest

sleep 3

platform_nightly_*/fluxd
