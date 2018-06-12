package org.influxdb.examples;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDBOptions;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;

public class ReactiveWriteExample {

    private static boolean stop = false;

    public static void main(String[] args) throws Exception {

        String databaseName = "influxdb_example";

        InfluxDBOptions options = InfluxDBOptions.builder()
                .username("admin")
                .password("admin")
                .database(databaseName)
                .url("http://localhost:8086").build();

        InfluxDBReactive client = InfluxDBReactiveFactory.connect(options);

        System.out.println("Write measurement every 2 Sec.");
        Flowable<ServerMeasurement> stats =
                Flowable.interval(2, TimeUnit.SECONDS)
                        .map(interval -> {
                            ServerMeasurement measurement = ServerMeasurement.getCurrentMeasurement();
                            System.out.println(interval + ": Get current host statistics: " + measurement);
                            return measurement;
                        }).takeWhile(val -> !stop);

        client.writeMeasurements(stats);
        Thread.sleep(10000);
        stop = true;

        client.close();

    }
}

