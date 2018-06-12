package org.influxdb.examples;

import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;
import org.influxdb.reactive.InfluxDBReactiveListenerDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackpressureWriteExample {

    public static void main(String[] args) {

        String databaseName = "backpressure_example";

        InfluxDBOptions options = InfluxDBOptions.builder()
                .username("admin")
                .password("admin")
                .database("backpressure_example")
                .url("http://localhost:8086").build();

        System.out.println("Simulate back pressure");

        BatchOptionsReactive batchOptionsReactive = BatchOptionsReactive.builder()
                .actions(100)
                .bufferLimit(300)
                .flushInterval(1000).build();

        BackPressureGenerator generator = new BackPressureGenerator();

        InfluxDBReactive client = InfluxDBReactiveFactory.connect(options, batchOptionsReactive,
                new InfluxDBReactiveListenerDefault() {
                    @Override
                    public void doOnBackpressure() {
                        System.out.println("BackPressure - slow down generator!");
                        generator.doOnBackpressure();
                    }
                });

        client.query(new Query("CREATE DATABASE \"" + databaseName + "\"", null)).subscribe();

        generator.run(client);

        client.close();
    }

    static class BackPressureGenerator {

        boolean tooFastEmmiting = false;
        long backPressureSleepDuration = 100;

        void run(InfluxDBReactive influxDBReactive) {

            for (int i = 0; i < 10000; i++) {
                if (tooFastEmmiting) {
                    try {
                        System.out.println("Slowing down generator by 1s. - " + i);
                        Thread.sleep(backPressureSleepDuration);
                        tooFastEmmiting = false;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                ServerMeasurement stats = ServerMeasurement.getCurrentMeasurement();
                influxDBReactive.writeMeasurement(stats);
            }
        }

        void doOnBackpressure() {
            tooFastEmmiting = true;
        }

    }

}
