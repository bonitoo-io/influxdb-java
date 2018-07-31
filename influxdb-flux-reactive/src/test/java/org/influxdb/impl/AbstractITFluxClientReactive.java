package org.influxdb.impl;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 08:20)
 */
public abstract class AbstractITFluxClientReactive {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxClientReactive.class.getName());

    protected static final String DATABASE_NAME = "flux_database";

    protected FluxClientReactiveImpl fluxClient;
    protected InfluxDB influxDB;

    @BeforeEach
    protected void setUp() {

        String fluxIP = System.getenv().getOrDefault("FLUX_IP", "127.0.0.1");
        String fluxPort = System.getenv().getOrDefault("FLUX_PORT_API", "8093");
        String fluxURL = "http://" + fluxIP + ":" + fluxPort;
        LOG.log(Level.FINEST, "Flux URL: {0}", fluxURL);

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url(fluxURL)
                .orgID("00")
                .build();

        fluxClient = new FluxClientReactiveImpl(fluxConnectionOptions);

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");
        String influxURL = "http://" + influxdbIP + ":" + influxdbPort;
        LOG.log(Level.FINEST, "Influx URL: {0}", influxURL);

        influxDB = InfluxDBFactory.connect(influxURL);
        influxDB.setDatabase(DATABASE_NAME);

        simpleQuery("CREATE DATABASE " + DATABASE_NAME);
    }

    @AfterEach
    protected void after() {

        simpleQuery("DROP DATABASE " + DATABASE_NAME);

        fluxClient.close();
    }

    protected void waitToFlux() {
        try {
            Thread.sleep(1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void simpleQuery(@Nonnull final String simpleQuery) {

        Objects.requireNonNull(simpleQuery, "SimpleQuery is required");
        QueryResult result = influxDB.query(new Query(simpleQuery, DATABASE_NAME));

        LOG.log(Level.FINEST, "Simple query: {0} result: {1}", new Object[]{simpleQuery, result});
    }
}