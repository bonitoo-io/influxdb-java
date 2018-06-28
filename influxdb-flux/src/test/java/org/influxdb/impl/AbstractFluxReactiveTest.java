package org.influxdb.impl;

import okhttp3.mockwebserver.MockWebServer;
import org.influxdb.flux.options.FluxOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 13:15)
 */
public abstract class AbstractFluxReactiveTest {

    protected MockWebServer fluxServer;
    protected FluxReactiveImpl fluxReactive;

    @BeforeEach
    protected void setUp() {

        fluxServer = new MockWebServer();
        try {
            fluxServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        FluxOptions fluxOptions = FluxOptions.builder()
                .url(fluxServer.url("/").url().toString())
                .orgID("0")
                .build();

        fluxReactive = new FluxReactiveImpl(fluxOptions);
    }

    @AfterEach
    protected void after() {
        fluxReactive.close();
    }
}
