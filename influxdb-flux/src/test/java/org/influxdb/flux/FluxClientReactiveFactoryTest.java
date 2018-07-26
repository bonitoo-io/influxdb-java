package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.options.FluxConnectionOptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:11)
 */
@RunWith(JUnitPlatform.class)
class FluxClientReactiveFactoryTest {
    
    @Test
    void connect() {

        FluxConnectionOptions fluxConnectionOptions = FluxConnectionOptions.builder()
                .url("http://localhost:8093")
                .orgID("00")
                .build();

        FluxClientReactive fluxClient = FluxClientReactiveFactory.connect(fluxConnectionOptions);

        Assertions.assertThat(fluxClient).isNotNull();
    }
}
