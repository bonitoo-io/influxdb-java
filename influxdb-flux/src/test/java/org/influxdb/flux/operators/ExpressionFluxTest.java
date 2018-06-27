package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 11:30)
 */
@RunWith(JUnitPlatform.class)
class ExpressionFluxTest {

    @Test
    void expression() {

        Flux flux = Flux.from("telegraf")
                .expression("map(fn: (r) => r._value * r._value)")
                .expression("sum()");

        String expected = "from(db:\"telegraf\") |> map(fn: (r) => r._value * r._value) |> sum()";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }
}