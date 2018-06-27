package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:03)
 */
@RunWith(JUnitPlatform.class)
class MinFluxTest {

    @Test
    void min() {

        Flux flux = Flux
                .from("telegraf")
                .min();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> min()");
    }

    @Test
    void minByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .min()
                .addNamedParameter("useStartTime", "parameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("parameter", true);

        Assertions.assertThat(flux.print(fluxChain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> min(useStartTime: true)");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .min(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> min(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .min(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> min(useStartTime: true)");
    }
}