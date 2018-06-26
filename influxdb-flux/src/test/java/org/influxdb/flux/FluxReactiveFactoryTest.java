package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.options.FluxOptions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 12:11)
 */
@RunWith(JUnitPlatform.class)
class FluxReactiveFactoryTest {
    
    @Test
    void connect() {

        FluxOptions fluxOptions = FluxOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

        FluxReactive fluxReactive = FluxReactiveFactory.connect(fluxOptions);

        Assertions.assertThat(fluxReactive).isNotNull();
    }
}
