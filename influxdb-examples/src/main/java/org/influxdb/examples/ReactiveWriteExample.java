package org.influxdb.examples;

import com.sun.management.OperatingSystemMXBean;
import io.reactivex.Flowable;
import java.util.concurrent.TimeUnit;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.impl.InfluxDBReactiveListenerDefault;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReactiveWriteExample {

    private static Logger LOG = LoggerFactory.getLogger(ReactiveWriteExample.class);

    @Measurement(name = "host")
    class HostStats {
        @Column(tag = true, name = "hostname")
        String hostname;
        @Column(name = "systemLoadAverage")
        double systemLoadAverage;
        @Column(name = "processCpuLoad")
        double processCpuLoad;
        @Column(name = "freePhysicalMemorySize")
        long freePhysicalMemorySize;

        @Override
        public String toString() {
            return "HostStats{" + "hostname='" + hostname + '\'' + ", systemLoadAverage=" + systemLoadAverage +
                    ", processCpuLoad=" + processCpuLoad + ", freePhysicalMemorySize=" + freePhysicalMemorySize + '}';
        }
    }

    private HostStats getStats() {
        HostStats m = new HostStats();
        m.hostname = Utils.getHostName();
        m.systemLoadAverage = Utils.getOperatingSystemMBean().getSystemLoadAverage();
        m.processCpuLoad = Utils.getOperatingSystemMBean().getProcessCpuLoad();
        m.freePhysicalMemorySize = Utils.getOperatingSystemMBean().getFreePhysicalMemorySize();
        return m;
    }


    public static void main(String[] args) {
        ReactiveWriteExample example = new ReactiveWriteExample();
        example.showWriteMeasurementsInInterval();
        example.showBackPressureWrite();
    }


    boolean stop = false;

    private void showWriteMeasurementsInInterval() {

        InfluxDBReactive influxDBReactive = Utils.createInfluxDBReactive();

        LOG.info("------------------------------");
        LOG.info("Write measurement every 2 Sec.");
        Flowable<HostStats> stats = Flowable.interval(2, TimeUnit.SECONDS).map(interval -> {
            HostStats current = new HostStats();
            current.hostname = Utils.getHostName();
            OperatingSystemMXBean bean = Utils.getOperatingSystemMBean();
            current.systemLoadAverage = bean.getSystemLoadAverage();
            current.processCpuLoad = bean.getSystemCpuLoad();
            current.freePhysicalMemorySize = bean.getFreePhysicalMemorySize();
            LOG.info("Get current host statistics: " + current);
            return current;
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

        InfluxDBReactive influxDBReactive = Utils.createInfluxDBReactive(optionsReactive, new InfluxDBReactiveListenerDefault() {
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
                HostStats stats = getStats();
                influxDBReactive.writeMeasurement(stats);
            }
        }

        void doOnBackpressure() {
            tooFastEmmiting = true;
        }

    }


}
