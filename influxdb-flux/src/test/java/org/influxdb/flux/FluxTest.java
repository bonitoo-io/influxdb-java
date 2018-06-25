package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:27)
 */
@RunWith(JUnitPlatform.class)
class FluxTest {

    @Test
    void flux1() {

        String flux = Flux
                .from("telegraf")
                .count()
                .print();

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }
}