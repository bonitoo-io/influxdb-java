package org.influxdb.flux.options.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.Instant;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 13:07)
 */
@RunWith(JUnitPlatform.class)
class NowOptionTest {

    @Test
    void byInstant() {

        NowOption nowOption = NowOption.builder()
                .time(Instant.ofEpochSecond(10_000))
                .build();

        Assertions.assertThat(nowOption.toString()).isEqualTo("option now = () => 1970-01-01T02:46:40.000000000Z");
    }

    @Test
    void byFunction() {

        NowOption nowOption = NowOption.builder()
                .function("giveMeTime()")
                .build();

        Assertions.assertThat(nowOption.toString()).isEqualToIgnoringWhitespace("option now = giveMeTime()");
    }

    @Test
    void withoutValue() {

        Assertions.assertThatThrownBy(() -> NowOption.builder().build())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("function or time has to be defined");
    }
}