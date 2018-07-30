package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (03/07/2018 15:05)
 */
@RunWith(JUnitPlatform.class)
class DerivativeFluxTest {

    @Test
    void derivative() {

        Flux flux = Flux
                .from("telegraf")
                .derivative(1L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 1m)");
    }

    @Test
    void derivativeByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                    .withUnit(10L, ChronoUnit.DAYS)
                    .withNonNegative(true)
                    .withColumns(new String[]{"time1", "time2"})
                    .withTimeSrc("_timeMy");

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 10d, nonNegative: true, columns: [\"time1\", \"time2\"], timeSrc: \"_timeMy\")");
    }

    @Test
    void derivativeByColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("century");

        Flux flux = Flux
                .from("telegraf")
                .derivative()
                    .withUnit(15L, ChronoUnit.DAYS)
                    .withColumns(columns);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> derivative(unit: 15d, columns: [\"time\", \"century\"])");
    }
}