#!/usr/bin/env bash


killall fluxd

docker stop flux-influxdb
docker rm flux-influxdb

docker stop flux-chronograf
docker rm flux-chronograf

docker stop flux-telegraf
docker rm flux-telegraf

docker network rm flux-net


