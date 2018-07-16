package org.influxdb.flux.options;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2018 14:05)
 */
@RunWith(JUnitPlatform.class)
class FluxQueryOptionsTest {

    @Test
    void defaultSettings() {

        Assertions.assertThat(FluxQueryOptions.DEFAULTS.getParserOptions().getValueDestinations())
                .hasSize(1)
                .containsExactlyInAnyOrder("_value");
    }

    @Test
    void customize() {

        FluxCsvParserOptions parserOptions = FluxCsvParserOptions
                .builder()
                .valueDestinations("val", "val2")
                .build();

        FluxQueryOptions queryOptions = FluxQueryOptions
                .builder()
                .parserOptions(parserOptions)
                .build();

        Assertions.assertThat(queryOptions.getParserOptions().getValueDestinations())
                .hasSize(2)
                .containsExactlyInAnyOrder("val", "val2");
    }
}