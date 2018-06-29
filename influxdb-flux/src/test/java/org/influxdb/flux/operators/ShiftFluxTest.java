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
 * @author Jakub Bednar (bednar@github) (29/06/2018 10:46)
 */
@RunWith(JUnitPlatform.class)
class ShiftFluxTest {

    @Test
    void shift() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS);

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> shift(shift: 10h)");
    }

    @Test
    void shiftColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, new String[]{"time", "custom"});

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> shift(shift: 10h, columns: [\"time\", \"custom\"])");
    }

    @Test
    void shiftColumnsCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("time");
        columns.add("_start");

        Flux flux = Flux
                .from("telegraf")
                .shift(10L, ChronoUnit.HOURS, columns);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> shift(shift: 10h, columns: [\"time\", \"_start\"])");
    }

    @Test
    void shiftByWith() {

        Flux flux = Flux
                .from("telegraf")
                .shift()
                .withShift(20L, ChronoUnit.DAYS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> shift(shift: 20d)");
    }
}