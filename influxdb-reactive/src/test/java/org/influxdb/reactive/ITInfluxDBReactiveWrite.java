package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.schedulers.TestScheduler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

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

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "http://127.0.0.1:8086");

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url(influxdbIP)
                .username("admin")
                .password("admin")
                .database(DATABASE_NAME)
                .build();

        influxDBCore = InfluxDBFactory.connect(options);
        influxDBCore.query(new Query("CREATE DATABASE " + DATABASE_NAME, null));

        influxDBReactive = new InfluxDBReactiveImpl(options, null);
    }

    @AfterEach
    void cleanUp() {
        influxDBReactive.close();
        influxDBCore.query(new Query("DROP DATABASE " + DATABASE_NAME, null));
        influxDBCore.close();
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

        // check result
        QueryResult queryResult = influxDBCore
                .query(new Query("select * from h2o_feet", "reactive_database"));

        InfluxDBResultMapper mapper = new InfluxDBResultMapper();
        List<H2OFeetMeasurement> measurements = mapper.toPOJO(queryResult, H2OFeetMeasurement.class);

        assertThat(measurements.size()).isEqualTo(5);

        assertThat(measurements.get(0).getLevel()).isEqualTo(0D);
        assertThat(measurements.get(1).getLevel()).isEqualTo(1D);
        assertThat(measurements.get(2).getLevel()).isEqualTo(2D);
        assertThat(measurements.get(3).getLevel()).isEqualTo(3D);
        assertThat(measurements.get(4).getLevel()).isEqualTo(4D);
    }
}