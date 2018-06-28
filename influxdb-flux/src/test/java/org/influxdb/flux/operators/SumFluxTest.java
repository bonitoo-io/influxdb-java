package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 10:22)
 */
@RunWith(JUnitPlatform.class)
class SumFluxTest {

    @Test
    void sum() {

        Flux flux = Flux
                .from("telegraf")
                .sum();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sum()");
    }

    @Test
    void sumByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .sum()
                .addPropertyNamed("useStartTime", "parameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("parameter", true);

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters)))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sum(useStartTime: true)");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .sum(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sum(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .sum(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sum(useStartTime: true)");
    }
}