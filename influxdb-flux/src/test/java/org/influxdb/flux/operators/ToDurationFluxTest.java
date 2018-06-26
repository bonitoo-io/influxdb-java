package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 06:30)
 */
@RunWith(JUnitPlatform.class)
class ToDurationFluxTest {

    @Test
    void toDuration() {

        Flux flux = Flux
                .from("telegraf")
                .toDuration();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> toDuration()");
    }
}