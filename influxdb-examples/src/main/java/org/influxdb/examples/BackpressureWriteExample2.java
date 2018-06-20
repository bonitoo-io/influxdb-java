package org.influxdb.examples;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveFactory;
import org.influxdb.reactive.event.BackpressureEvent;

import java.util.concurrent.TimeUnit;

public class BackpressureWriteExample2 {

    public static void main(String[] args) {

        String databaseName = "backpressure_example";

        InfluxDBOptions options = InfluxDBOptions.builder()
                .username("admin")
                .password("admin")
                .database(databaseName)
                .url("http://localhost:8086").build();

        BatchOptionsReactive batchOptionsReactive = BatchOptionsReactive.builder()
                .batchSize(100)
                .bufferLimit(1000)
                .backpressureStrategy(BackpressureOverflowStrategy.DROP_OLDEST)
                .flushInterval(1000).build();

        ServerMeasurementGenerator metricsGenerator = new ServerMeasurementGenerator();
        metricsGenerator.backPressureDelay = 1000;
        metricsGenerator.numberOfEvents = 10000;

        InfluxDBReactive client = InfluxDBReactiveFactory.connect(options, batchOptionsReactive);
        client.listenEvents(BackpressureEvent.class).subscribe(metricsGenerator);

        client.query(new Query("CREATE DATABASE \"" + databaseName + "\"", null)).blockingSubscribe();
        client.query(new Query("DROP MEASUREMENT \"host\"", databaseName)).blockingSubscribe();

        client.writeMeasurements(metricsGenerator.metrics());
        client.close();
    }

    static class ServerMeasurementGenerator implements Consumer<BackpressureEvent> {

        int numberOfEvents;
        int backPressureDelay;
        int delay;

        Flowable<ServerMeasurement> metrics() {

            return Flowable.range(0, numberOfEvents)
                    //display info for each 100 iteration
                    .map(i -> {
                        if (i % 100 == 0) {
                            System.out.println("#" + i + " points");
                        }
                        return i;
                    })
                    //obtain current measurement
                    .flatMap(i -> Flowable.fromCallable(ServerMeasurement::getCurrentMeasurement))
                    //apply delay on backpressure
                    .delay(serverMeasurement -> {
                        if (delay == 0) {
                            return Flowable.empty();
                        }
                        System.out.println("On backpressure, pause for " + delay + "ms.");
                        Flowable<Long> timer = Flowable.timer(delay, TimeUnit.MILLISECONDS, Schedulers.trampoline());
                        delay = 0;
                        return timer;
                    });
        }


        @Override
        public void accept(BackpressureEvent backpressureEvent) {
            delay = backPressureDelay;
        }
    }


}
