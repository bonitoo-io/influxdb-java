package org.influxdb.flux.options.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (27/07/2018 07:53)
 */
@RunWith(JUnitPlatform.class)
class CustomOptionTest {

    @Test
    void customOption() {

        CustomOption customOption = CustomOption.builder("end").value("() => now()").build();

        Assertions.assertThat(customOption.toString()).isEqualTo("option end = () => now()");
    }

    @Test
    void withoutValue() {

        Assertions.assertThatThrownBy(() -> CustomOption.builder("end").build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Option value has to be defined");
    }
}