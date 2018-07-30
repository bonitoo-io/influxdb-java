package org.influxdb.flux.custom;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (02/07/2018 13:55)
 */
@RunWith(JUnitPlatform.class)
class CustomFunction {

    @Test
    void customFunction() {

        Flux flux = Flux
                .from("telegraf")
                .operator(FilterMeasurement.class)
                    .withName("cpu")
                .sum();

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> measurement(m:\"cpu\") |> sum()");
    }

}