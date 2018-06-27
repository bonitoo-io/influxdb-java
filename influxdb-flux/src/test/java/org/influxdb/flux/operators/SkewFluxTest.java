package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:08)
 */
@RunWith(JUnitPlatform.class)
class SkewFluxTest {

    @Test
    void skew() {

        Flux flux = Flux
                .from("telegraf")
                .skew();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> skew()");
    }

    @Test
    void skewByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .skew()
                .addNamedParameter("useStartTime", "parameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("parameter", true);

        Assertions.assertThat(flux.print(fluxChain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> skew(useStartTime: true)");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .skew(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> skew(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .skew(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> skew(useStartTime: true)");
    }
}