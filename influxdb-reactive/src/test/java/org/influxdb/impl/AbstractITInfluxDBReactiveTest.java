package org.influxdb.impl;

import io.reactivex.schedulers.TestScheduler;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.InfluxDBReactive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.TimeUnit;

import static org.influxdb.reactive.BatchOptionsReactive.DISABLED;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:14)
 */
public abstract class AbstractITInfluxDBReactiveTest {

    private static final String DATABASE_NAME = "reactive_database";

    protected InfluxDB influxDBCore;
    protected InfluxDBReactive influxDBReactive;

    @BeforeEach
    void setUp() {

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://" + influxdbIP + ":" + influxdbPort)
                .username("admin")
                .password("admin")
                .database(DATABASE_NAME)
                .precision(TimeUnit.MILLISECONDS)
                .build();

        influxDBCore = InfluxDBFactory.connect(options);
        influxDBCore.query(new Query("CREATE DATABASE " + DATABASE_NAME, null));

        InfluxDBReactiveListenerVerifier verifier = new InfluxDBReactiveListenerVerifier();
        TestScheduler batchScheduler = new TestScheduler();
        TestScheduler jitterScheduler = new TestScheduler();

        influxDBReactive = new InfluxDBReactiveImpl(
                options, DISABLED,
                batchScheduler, jitterScheduler,
                null,
                verifier);
    }

    @AfterEach
    void cleanUp() {
        influxDBReactive.close();
        influxDBCore.query(new Query("DROP DATABASE " + DATABASE_NAME, null));
        influxDBCore.close();
    }
}
