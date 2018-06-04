package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.schedulers.TestScheduler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBReactiveImpl;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 10:06)
 */
@RunWith(JUnitPlatform.class)
class ITInfluxDBReactiveWrite {

    private static final String DATABASE_NAME = "reactive_database";

    private InfluxDB influxDBCore;
    private InfluxDBReactive influxDBReactive;

    @BeforeEach
    void setUp() {

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://" + influxdbIP + ":" + influxdbPort)
                .username("admin")
                .password("admin")
                .database(DATABASE_NAME)
                .build();

        influxDBCore = InfluxDBFactory.connect(options);
        influxDBCore.query(new Query("CREATE DATABASE " + DATABASE_NAME, null));

        influxDBReactive = new InfluxDBReactiveImpl(options, BatchOptionsReactive.DISABLED, null);
    }

    @AfterEach
    void cleanUp() {
        influxDBReactive.close();
        influxDBCore.query(new Query("DROP DATABASE " + DATABASE_NAME, null));
        influxDBCore.close();
    }

    @Test
    void write() {

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046801L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049802L);

        // write
        influxDBReactive.writeMeasurements(Flowable.just(measurement1, measurement2));

        // get from DB
        List<H2OFeetMeasurement> measurements = getMeasurements();
        assertThat(measurements.size()).isEqualTo(2);

        // measurement 1
        assertThat(measurements.get(0).getLocation()).isEqualTo("coyote_creek");
        assertThat(measurements.get(0).getLevel()).isEqualTo(2.927);
        assertThat(measurements.get(0).getDescription()).isEqualTo("below 3 feet");
        assertThat(measurements.get(0).getTime().toEpochMilli()).isEqualTo(1440046801L);

        // measurement 2
        assertThat(measurements.get(1).getLocation()).isEqualTo("coyote_creek");
        assertThat(measurements.get(1).getLevel()).isEqualTo(1.927);
        assertThat(measurements.get(1).getDescription()).isEqualTo("below 2 feet");
        assertThat(measurements.get(1).getTime().toEpochMilli()).isEqualTo(1440049802L);
    }

    @Test
    void publishPattern() {

        TestScheduler scheduler = new TestScheduler();

        // every 10 seconds publish new h2o level
        Flowable<H2OFeetMeasurement> observeWeather = Flowable.interval(10, TimeUnit.SECONDS, scheduler)
                .map(time -> {

                    double h2oLevel = time.doubleValue();
                    long timestamp = System.currentTimeMillis();

                    return new H2OFeetMeasurement(
                            "coyote_creek", h2oLevel, "from ocean sensor", timestamp);
                });

        // write
        influxDBReactive.writeMeasurements(observeWeather);

        // 50 seconds to feature
        scheduler.advanceTimeBy(50, TimeUnit.SECONDS);

        // get from DB
        List<H2OFeetMeasurement> measurements = getMeasurements();
        assertThat(measurements.size()).isEqualTo(5);

        assertThat(measurements.get(0).getLevel()).isEqualTo(0D);
        assertThat(measurements.get(1).getLevel()).isEqualTo(1D);
        assertThat(measurements.get(2).getLevel()).isEqualTo(2D);
        assertThat(measurements.get(3).getLevel()).isEqualTo(3D);
        assertThat(measurements.get(4).getLevel()).isEqualTo(4D);
    }

    @Nonnull
    private List<H2OFeetMeasurement> getMeasurements() {

        QueryResult queryResult = influxDBCore
                .query(new Query("select * from h2o_feet", "reactive_database"));

        return new InfluxDBResultMapper().toPOJO(queryResult, H2OFeetMeasurement.class);
    }
}