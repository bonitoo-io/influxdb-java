package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;

/**
 * @author Jakub Bednar (bednar@github) (03/07/2018 12:48)
 */
@RunWith(JUnitPlatform.class)
class IntegralFluxTest {

    @Test
    void integral() {

        Flux flux = Flux
                .from("telegraf")
                .integral(1L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> integral(unit: 1m)");
    }

    @Test
    void integralByParameter() {

        Flux flux = Flux
                .from("telegraf")
                .integral()
                    .withUnit(5L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> integral(unit: 5m)");
    }
}