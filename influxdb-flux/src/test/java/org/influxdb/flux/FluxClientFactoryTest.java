package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 13:12)
 */
@RunWith(JUnitPlatform.class)
class FluxClientFactoryTest {

    @Test
    void connect() {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

        FluxClient fluxClient = FluxClientFactory.connect(options);

        Assertions.assertThat(fluxClient).isNotNull();
    }

    @Test
    void f() {

        FluxConnectionOptions options = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

// Results
        FluxClient fluxClient = FluxClientFactory.connect(options);

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference();

        fluxClient.flux(flux, fluxResult -> {

            logFluxResult(fluxResult);
        });

        fluxClient.close();
    }


    private void logFluxResult(FluxResult fluxResult) {
        //To change body of created methods use File | Settings | File Templates.
    }
}