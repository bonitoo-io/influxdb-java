package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (26/06/2018 07:23)
 */
@RunWith(JUnitPlatform.class)
class RangeFluxTest {

    @Test
    void startInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000));

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> range(start: 1970-01-18T08:40:00.000000000Z)");
    }

    @Test
    void startStopInstant() {

        Flux flux = Flux
                .from("telegraf")
                .range(Instant.ofEpochSecond(1_500_000), Instant.ofEpochSecond(2_000_000));

        String expected = "from(db:\"telegraf\") |> "
                + "range(start: 1970-01-18T08:40:00.000000000Z, stop: 1970-01-24T03:33:20.000000000Z)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void startUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, TimeUnit.SECONDS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> range(start: 15s)");
    }

    @Test
    void startUnitNegative() {

        Flux flux = Flux
                .from("telegraf")
                .range(-33L, TimeUnit.HOURS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> range(start: -33h)");
    }

    @Test
    void startStopUnit() {

        Flux flux = Flux
                .from("telegraf")
                .range(15L, 44L, TimeUnit.NANOSECONDS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> range(start: 15n, stop: 44n)");
    }

    @Test
    void startParameter() {

        Flux flux = Flux
                .from("telegraf")
                .range("startParameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("startParameter", Instant.ofEpochSecond(1_600_000));

        Assertions.assertThat(flux.print(fluxChain))
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> range(start: 1970-01-19T12:26:40.000000000Z)");
    }

    @Test
    void startStopParameter() {

        Flux flux = Flux
                .from("telegraf")
                .range("startParameter", "stopParameter");

        FluxChain fluxChain = new FluxChain()
                .addParameter("startParameter", Instant.ofEpochSecond(1_600_000))
                .addParameter("stopParameter", Instant.ofEpochSecond(1_800_000));

        String expected = "from(db:\"telegraf\") |> "
                + "range(start: 1970-01-19T12:26:40.000000000Z, stop: 1970-01-21T20:00:00.000000000Z)";

        Assertions.assertThat(flux.print(fluxChain)).isEqualToIgnoringWhitespace(expected);
    }
}