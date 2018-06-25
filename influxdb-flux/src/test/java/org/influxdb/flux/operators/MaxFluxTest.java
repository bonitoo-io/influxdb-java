package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:52)
 */
@RunWith(JUnitPlatform.class)
class MaxFluxTest {

    @Test
    void max() {

        Flux flux = Flux
                .from("telegraf")
                .max();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> max()");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .max(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> max(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .max(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> max(useStartTime: true)");
    }
}