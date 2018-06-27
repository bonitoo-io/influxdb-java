package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:58)
 */
@RunWith(JUnitPlatform.class)
class MeanFluxTest {

    @Test
    void mean() {

        Flux flux = Flux
                .from("telegraf")
                .mean();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> mean()");
    }

    @Test
    void meanByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .mean()
                .addNamedParameter("useStartTime", "parameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("parameter", true);

        Assertions.assertThat(flux.print(fluxChain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> mean(useStartTime: true)");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .mean(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> mean(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .mean(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> mean(useStartTime: true)");
    }
}
