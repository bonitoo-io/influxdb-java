package org.influxdb.impl;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:14)
 */
public abstract class AbstractITInfluxDBReactiveTest {

    protected static final String DATABASE_NAME = "reactive_database";

    protected InfluxDB influxDBCore;

    protected InfluxDBReactive influxDBReactive;
    protected InfluxDBReactiveListenerVerifier verifier;

    protected void setUp(@Nonnull final BatchOptionsReactive batchOptions) {

        Objects.requireNonNull(batchOptions, "BatchOptionsReactive is required");

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

        verifier = new InfluxDBReactiveListenerVerifier();

        influxDBReactive = new InfluxDBReactiveImpl(options, batchOptions, verifier);
    }

    @AfterEach
    void cleanUp() {
        influxDBReactive.close();
        influxDBCore.query(new Query("DROP DATABASE " + DATABASE_NAME, null));
        influxDBCore.close();
    }
}
