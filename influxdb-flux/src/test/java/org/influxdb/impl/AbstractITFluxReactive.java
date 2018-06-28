package org.influxdb.impl;

import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.flux.options.FluxOptions;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 08:20)
 */
public abstract class AbstractITFluxReactive {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxReactive.class.getName());

    protected static final String DATABASE_NAME = "flux_database";

    protected FluxReactiveImpl fluxReactive;
    protected InfluxDBReactiveImpl influxDBReactive;

    @BeforeEach
    protected void setUp() {

        String fluxIP = System.getenv().getOrDefault("FLUX_IP", "127.0.0.1");
        String fluxPort = System.getenv().getOrDefault("FLUX_PORT_API", "8093");

        FluxOptions fluxOptions = FluxOptions.builder()
                .url("http://" + fluxIP + ":" + fluxPort)
                .orgID("00")
                .build();

        fluxReactive = new FluxReactiveImpl(fluxOptions);

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");
        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://" + influxdbIP + ":" + influxdbPort)
                .username("admin")
                .password("admin")
                .database(DATABASE_NAME)
                .precision(TimeUnit.SECONDS)
                .build();

        influxDBReactive = new InfluxDBReactiveImpl(options, BatchOptionsReactive.DISABLED);

        simpleQuery("CREATE DATABASE " + DATABASE_NAME);
    }

    @AfterEach
    protected void after() {

        simpleQuery("DROP DATABASE " + DATABASE_NAME);

        fluxReactive.close();
        influxDBReactive.close();
    }

    protected void waitToSecondsTo(@Nonnull final BooleanSupplier supplier) {

        long start = System.currentTimeMillis();
        while (!supplier.getAsBoolean()) {
            if (System.currentTimeMillis() - start > 10_000) {
                throw new RuntimeException("Condition was not success in 10 seconds.");
            }
        }
    }

    protected void waitToFlux()
    {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void simpleQuery(@Nonnull final String simpleQuery) {

        Objects.requireNonNull(simpleQuery, "SimpleQuery is required");
        QueryResult result = influxDBReactive.query(new Query(simpleQuery, null)).blockingSingle();

        LOG.log(Level.FINEST, "Simple query: {0} result: {1}", new Object[]{simpleQuery, result});
    }
}