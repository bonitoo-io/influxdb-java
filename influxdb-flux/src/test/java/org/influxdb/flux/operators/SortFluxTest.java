package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (25/06/2018 13:54)
 */
@RunWith(JUnitPlatform.class)
class SortFluxTest {

    @Test
    void sortByColumnsCollection() {

        List<String> sortBy = new ArrayList<>();
        sortBy.add("region");
        sortBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .sort(sortBy);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sort(cols: [\"region\", \"host\"])");
    }

    @Test
    void sortByColumnsArray() {

        Flux flux = Flux
                .from("telegraf")
                .sort(new String[]{"region", "value"});

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sort(cols: [\"region\", \"value\"])");
    }

    @Test
    void descendingSort() {
        Flux flux = Flux
                .from("telegraf")
                .sort(true);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sort(desc: true)");
    }

    @Test
    void descendingSortFalse() {

        Flux flux = Flux
                .from("telegraf")
                .sort(false);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> sort(desc: false)");
    }

    @Test
    void sortByColumnsDescending() {

        Flux flux = Flux
                .from("telegraf")
                .sort(new String[]{"region", "value"}, true);

        String expected = "from(db:\"telegraf\") |> sort(cols: [\"region\", \"value\"], desc: true)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sortByColumnsAscending() {

        List<String> sortBy = new ArrayList<>();
        sortBy.add("region");
        sortBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .sort(sortBy, false);

        String expected = "from(db:\"telegraf\") |> sort(cols: [\"region\", \"host\"], desc: false)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sortByParameters() {

        Flux flux = Flux
                .from("telegraf")
                .sort()
                .addNamedParameter("cols", "columnsParameter")
                .addNamedParameter("desc", "descParameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("columnsParameter", new String[]{"region", "tag"})
                .addParameter("descParameter", false);

        String expected = "from(db:\"telegraf\") |> sort(cols: [\"region\", \"tag\"], desc: false)";

        Assertions.assertThat(flux.print(fluxChain)).isEqualToIgnoringWhitespace(expected);
    }
}