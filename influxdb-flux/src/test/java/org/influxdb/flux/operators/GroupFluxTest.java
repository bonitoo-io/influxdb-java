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
 * @author Jakub Bednar (bednar@github) (25/06/2018 15:16)
 */
@RunWith(JUnitPlatform.class)
class GroupFluxTest {

    @Test
    void groupByCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupBy(groupBy);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(by: [\"region\", \"host\"])");
    }

    @Test
    void groupByKeepCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        List<String> keepBy = new ArrayList<>();
        keepBy.add("server");
        keepBy.add("state");

        Flux flux = Flux
                .from("telegraf")
                .groupBy(groupBy, keepBy);

        String expected = "from(db:\"telegraf\") |> group(by: [\"region\", \"host\"], keep: [\"server\", \"state\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupByParameter() {

        FluxChain chain = new FluxChain()
                .addParameter("groupByParameter", new String[]{"region", "zip"});

        Flux flux = Flux
                .from("telegraf")
                .groupBy("groupByParameter");

        Assertions.assertThat(flux.print(chain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(by: [\"region\", \"zip\"])");
    }

    @Test
    void groupByArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy(new String[]{"region", "value"});

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(by: [\"region\", \"value\"])");
    }


    @Test
    void groupByKeepArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupBy(new String[]{"region", "value"}, new String[]{"server", "rack"});

        String expected = "from(db:\"telegraf\") |> group(by: [\"region\", \"value\"], keep: [\"server\", \"rack\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupByKeepParameter() {

        FluxChain chain = new FluxChain()
                .addParameter("groupByParameter", new String[]{"region", "zip"})
                .addParameter("keepByParameter", new String[]{"server", "price"});

        Flux flux = Flux
                .from("telegraf")
                .groupBy("groupByParameter", "keepByParameter");

        String expected = "from(db:\"telegraf\") |> group(by: [\"region\", \"zip\"], keep: [\"server\", \"price\"])";

        Assertions.assertThat(flux.print(chain)).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(groupBy);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(except: [\"region\", \"host\"])");
    }

    @Test
    void groupExceptKeepCollection() {

        List<String> groupBy = new ArrayList<>();
        groupBy.add("region");
        groupBy.add("host");

        List<String> keepBy = new ArrayList<>();
        keepBy.add("server");
        keepBy.add("state");

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(groupBy, keepBy);

        String expected = "from(db:\"telegraf\") |> group(except: [\"region\", \"host\"], keep: [\"server\", \"state\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptParameter() {

        FluxChain chain = new FluxChain()
                .addParameter("groupExceptParameter", new String[]{"region", "zip"});

        Flux flux = Flux
                .from("telegraf")
                .groupExcept("groupExceptParameter");

        Assertions.assertThat(flux.print(chain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(except: [\"region\", \"zip\"])");
    }

    @Test
    void groupExceptArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(new String[]{"region", "value"});

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> group(except: [\"region\", \"value\"])");
    }


    @Test
    void groupExceptKeepArray() {

        Flux flux = Flux
                .from("telegraf")
                .groupExcept(new String[]{"region", "value"}, new String[]{"server", "rack"});

        String expected = "from(db:\"telegraf\") |> group(except: [\"region\", \"value\"], keep: [\"server\", \"rack\"])";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void groupExceptKeepParameter() {

        FluxChain chain = new FluxChain()
                .addParameter("groupExceptParameter", new String[]{"region", "zip"})
                .addParameter("keepExceptParameter", new String[]{"server", "price"});

        Flux flux = Flux
                .from("telegraf")
                .groupExcept("groupExceptParameter", "keepExceptParameter");

        String expected = "from(db:\"telegraf\") |> group(except: [\"region\", \"zip\"], keep: [\"server\", \"price\"])";

        Assertions.assertThat(flux.print(chain)).isEqualToIgnoringWhitespace(expected);
    }
}