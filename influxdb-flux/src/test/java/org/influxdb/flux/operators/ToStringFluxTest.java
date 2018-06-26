package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 06:35)
 */
@RunWith(JUnitPlatform.class)
class ToStringFluxTest {

    @Test
    void toBool() {

        Flux flux = Flux
                .from("telegraf")
                .toStringConvert();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> toString()");
    }
}