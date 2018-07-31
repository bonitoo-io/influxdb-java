package org.influxdb.impl;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.flux.FluxClient;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:35)
 */
public abstract class AbstractITFluxClient extends AbstractTest {

    private static final Logger LOG = Logger.getLogger(AbstractITFluxClient.class.getName());

    protected static final String DATABASE_NAME = "flux_database";

    protected FluxClient fluxClient;
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

        fluxClient = new FluxClientImpl(fluxConnectionOptions);

        String influxdbIP = System.getenv().getOrDefault("INFLUXDB_IP", "127.0.0.1");
        String influxdbPort = System.getenv().getOrDefault("INFLUXDB_PORT_API", "8086");
        String influxURL = "http://" + influxdbIP + ":" + influxdbPort;
        LOG.log(Level.FINEST, "Influx URL: {0}", influxURL);

        influxDB = InfluxDBFactory.connect(influxURL);
        influxDB.setDatabase(DATABASE_NAME);
        influxDB.query(new Query("CREATE DATABASE "+ DATABASE_NAME, DATABASE_NAME));
    }

    @AfterEach
    protected void after() {

        influxDB.query(new Query("DROP DATABASE "+ DATABASE_NAME, DATABASE_NAME));

        fluxClient.close();
        fluxClient.close();
    }
}