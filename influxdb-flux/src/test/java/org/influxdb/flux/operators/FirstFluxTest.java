package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.HashMap;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 09:40)
 */
@RunWith(JUnitPlatform.class)
class FirstFluxTest {

    @Test
    void first() {

        Flux flux = Flux
                .from("telegraf")
                .first();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> first()");
    }

    @Test
    void firstByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .first()
                .addPropertyNamed("useStartTime", "parameter");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("parameter", true);

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters)))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> first(useStartTime: true)");
    }

    @Test
    void useStartTimeFalse() {

        Flux flux = Flux
                .from("telegraf")
                .first(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> first(useStartTime: false)");
    }

    @Test
    void useStartTimeTrue() {

        Flux flux = Flux
                .from("telegraf")
                .first(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> first(useStartTime: true)");
    }
}