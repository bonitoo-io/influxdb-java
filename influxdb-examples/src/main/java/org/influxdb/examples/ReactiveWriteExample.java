package org.influxdb.examples;

import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.InfluxDBReactiveListenerDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactiveWriteExample {

    private static Logger LOG = LoggerFactory.getLogger(ReactiveWriteExample.class);

    public static void main(String[] args) {
        ReactiveWriteExample example = new ReactiveWriteExample();
        example.showWriteMeasurementsInInterval();
        example.showBackPressureWrite();
    }

    private boolean stop = false;

    private void showWriteMeasurementsInInterval() {

        InfluxDBReactive influxDBReactive = Utils.createInfluxDBReactive();

        LOG.info("------------------------------");
        LOG.info("Write measurement every 2 Sec.");
        Flowable<ServerMeasurement> stats = Flowable.interval(2, TimeUnit.SECONDS).map(interval -> {
            ServerMeasurement measurement = ServerMeasurement.getCurrentMeasurement();
            LOG.info(interval + ": Get current host statistics: " + measurement);
            return measurement;
        }).takeWhile(val -> !stop);

        influxDBReactive.writeMeasurements(stats);
        Utils.sleep(10000);
        stop = true;

        influxDBReactive.close();
    }

    private void showBackPressureWrite() {

        LOG.info("------------------------------");
        LOG.info("Simulate back pressure");

        BatchOptionsReactive optionsReactive = BatchOptionsReactive.builder()
                .actions(100)
                .bufferLimit(300)
                .flushInterval(1000).build();

        BackPressureGenerator generator = new BackPressureGenerator();

        InfluxDBReactive influxDBReactive = Utils.createInfluxDBReactive(optionsReactive,
                new InfluxDBReactiveListenerDefault() {
                    @Override
                    public void doOnBackpressure() {
                        LOG.info("BackPressure - slow down generator!");
                        generator.doOnBackpressure();
                    }
                });

        generator.run(influxDBReactive);

        influxDBReactive.close();
    }

    class BackPressureGenerator {

        boolean tooFastEmmiting = false;
        long backPressureSleepDuration = 100;

        void run(InfluxDBReactive influxDBReactive) {

            for (int i = 0; i < 10000; i++) {
                if (tooFastEmmiting) {
                    try {
                        LOG.info("Slowing down generator by 1s. - " + i);
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
