package org.influxdb.impl;

import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.reactive.BatchOptionsReactive;
import org.influxdb.reactive.InfluxDBReactive;
import org.junit.jupiter.api.AfterEach;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:14)
 */
public abstract class AbstractITInfluxDBReactiveTest {

    private static final Logger LOG = Logger.getLogger(AbstractITInfluxDBReactiveTest.class.getName());

    protected static final String DATABASE_NAME = "reactive_database";

    protected InfluxDBReactive influxDBReactive;
    protected InfluxDBReactiveVerifier verifier;

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


        influxDBReactive = new InfluxDBReactiveImpl(options, batchOptions);
        verifier = new InfluxDBReactiveVerifier(influxDBReactive);

        simpleQuery("CREATE DATABASE " + DATABASE_NAME);

        verifier.reset();
    }

    @AfterEach
    void cleanUp() {
        simpleQuery("DROP DATABASE " + DATABASE_NAME);

        influxDBReactive.close();
    }

    protected void simpleQuery(@Nonnull final String simpleQuery) {

        Objects.requireNonNull(simpleQuery, "SimpleQuery is required");
        QueryResult result = influxDBReactive.query(new Query(simpleQuery, null)).blockingSingle();

        LOG.log(Level.FINEST, "Simple query: {0} result: {1}", new Object[]{simpleQuery, result});
    }
}
