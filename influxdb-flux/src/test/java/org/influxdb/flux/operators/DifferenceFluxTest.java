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
 * @author Jakub Bednar (bednar@github) (17/07/2018 12:57)
 */
@RunWith(JUnitPlatform.class)
class DifferenceFluxTest {

    @Test
    void differenceNonNegative() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(false);

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference(nonNegative: false)";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceStringCollection() {

        List<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(columns);

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference(columns: [\"_time\", \"_value\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceArray() {

        Flux flux = Flux
                .from("telegraf")
                .range(-5L, ChronoUnit.MINUTES)
                .difference(new String[]{"_value", "_time"});

        String expected = "from(db:\"telegraf\") |> range(start: -5m) |> difference(columns: [\"_value\", \"_time\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceStringCollectionNonNegative() {

        List<String> columns = new ArrayList<>();
        columns.add("_time");
        columns.add("_value");

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(columns, true);

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference(columns: [\"_time\", \"_value\"], nonNegative: true)";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceArrayNonNegative() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference(new String[]{"_val", "_time"}, false);

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference(columns: [\"_val\", \"_time\"], nonNegative: false)";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void differenceByProperty() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference()
                .withColumns(new String[]{"_value", "_oldValue"});


        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference(columns: [\"_value\", \"_oldValue\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void onlyDifference() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy("_measurement")
                .difference();

        String expected = "from(db:\"telegraf\") |> group(by: [\"_measurement\"]) |> difference()";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }
}