package org.influxdb.flux.options;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 09:09)
 */
@RunWith(JUnitPlatform.class)
class FluxOptionsTest {

    @Test
    void value() {
        FluxOptions fluxOptions = FluxOptions.builder()
                .url("http://localhost:8093")
                .orgID("0")
                .build();

        Assertions.assertThat(fluxOptions.getUrl()).isEqualTo("http://localhost:8093");
        Assertions.assertThat(fluxOptions.getOrgID()).isEqualTo("0");
        Assertions.assertThat(fluxOptions.isNotDefined()).isFalse();
    }

    @Test
    void notDefinedOptions() {
        Assertions.assertThat(FluxOptions.NOT_DEFINED.isNotDefined()).isTrue();
    }

    @Test
    void urlRequired() {

        FluxOptions.Builder fluxOptions = FluxOptions.builder().orgID("0");

        Assertions.assertThatThrownBy(fluxOptions::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The url to connect to Flux has to be defined.");
    }

    @Test
    void orgIdRequired() {

        FluxOptions.Builder fluxOptions = FluxOptions.builder().url("http://localhost:8093");

        Assertions.assertThatThrownBy(fluxOptions::build)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The organization id required by Flux has to be defined.");
    }
}