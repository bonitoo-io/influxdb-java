package org.influxdb.examples;

import io.reactivex.functions.Consumer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;
import org.influxdb.reactive.event.BackpressureEvent;

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
                .batchSize(100)
                .bufferLimit(300)
                .flushInterval(1000).build();

        BackPressureGenerator generator = new BackPressureGenerator();

        InfluxDBReactive client = InfluxDBReactiveFactory.connect(options, batchOptionsReactive);
        client.listenEvents(BackpressureEvent.class).subscribe(generator) ;

        client.query(new Query("CREATE DATABASE \"" + databaseName + "\"", null)).subscribe();

        generator.run(client);

        client.close();
    }

    static class BackPressureGenerator implements Consumer<BackpressureEvent> {

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

        @Override
        public void accept(BackpressureEvent backpressureEvent) {

            System.out.println("BackPressure - slow down generator!");
            tooFastEmmiting = true;
        }
    }

}
