package org.influxdb.flux.options;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (16/07/2018 13:01)
 */
@RunWith(JUnitPlatform.class)
class FluxCsvParserOptionsTest {

    @Test
    void defaultSettings() {

        Assertions.assertThat(FluxCsvParserOptions.DEFAULTS.getValueDestinations())
                .hasSize(1)
                .containsExactlyInAnyOrder("_value");
    }

    @Test
    void emptyValueDestinations() {

        FluxCsvParserOptions settings = FluxCsvParserOptions
                .builder()
                .valueDestinations()
                .build();

        Assertions.assertThat(settings.getValueDestinations())
                .hasSize(1)
                .containsExactlyInAnyOrder("_value");
    }

    @Test
    void customizeValueDestinations() {

        FluxCsvParserOptions settings = FluxCsvParserOptions
                .builder()
                .valueDestinations("value1", "_value2")
                .build();

        Assertions.assertThat(settings.getValueDestinations())
                .hasSize(2)
                .containsExactlyInAnyOrder("value1", "_value2");
    }
}