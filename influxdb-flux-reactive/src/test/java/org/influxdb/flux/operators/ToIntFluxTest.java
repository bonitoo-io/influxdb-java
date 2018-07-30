package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 16:09)
 */
@RunWith(JUnitPlatform.class)
class ToIntFluxTest {

    @Test
    void toInt() {

        Flux flux = Flux
                .from("telegraf")
                .toInt();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> toInt()");
    }
}