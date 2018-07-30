package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:03)
 */
@RunWith(JUnitPlatform.class)
class YieldFluxTest {

    @Test
    void yield() {

        Flux flux = Flux
                .from("telegraf")
                .yield("0");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> yield(name: \"0\")");
    }

    @Test
    void yieldByWith() {

        Flux flux = Flux
                .from("telegraf")
                .yield()
                .withName("1");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> yield(name: \"1\")");
    }
}