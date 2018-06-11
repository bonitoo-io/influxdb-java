package org.influxdb.examples;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import org.influxdb.reactive.InfluxDBReactive;

public class SimpleReactiveExample {


    public static void main(String[] args) {

        //create client
        InfluxDBReactive client = Utils.createInfluxDBReactive();

        //write bean into influxDB
        ServerMeasurement serverMeasurement = ServerMeasurement.getCurrentMeasurement();
        client.writeMeasurement(serverMeasurement);


        Flowable<ServerMeasurement> serverInfos =
                Flowable.interval(1, TimeUnit.SECONDS).map(i -> {
                    ServerMeasurement currentServerMeasurement = ServerMeasurement.getCurrentMeasurement();
                    System.out.println(currentServerMeasurement);
                    return currentServerMeasurement;
                });

        client.writeMeasurements(serverInfos);

        Utils.sleep(5000);


    }
}
