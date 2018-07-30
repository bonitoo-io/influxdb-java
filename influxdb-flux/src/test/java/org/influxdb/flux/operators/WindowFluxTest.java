package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.operators.properties.TimeInterval;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * @author Jakub Bednar (bednar@github) (27/06/2018 12:42)
 */
@RunWith(JUnitPlatform.class)
class WindowFluxTest {

    @Test
    void windowEveryChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 15m)");
    }

    @Test
    void unSupportedChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.DECADES);

        Assertions.assertThatThrownBy(flux::print)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unit must be one of: NANOS, MICROS, MILLIS, SECONDS, MINUTES, HOURS, HALF_DAYS, DAYS, WEEKS");
    }

    @Test
    void windowEveryPeriodChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 15m, period: 20s)");
    }

    @Test
    void windowEveryPeriodStartChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.HALF_DAYS, 20L, ChronoUnit.SECONDS, -50L, ChronoUnit.DAYS);

        Assertions.assertThat(flux.print())
                .isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> window(every: 180h, period: 20s, start: -50d)");
    }

    @Test
    void windowEveryPeriodStartInstant() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES, 20L, ChronoUnit.SECONDS, Instant.ofEpochSecond(1_750_000));

        String expected = "from(db:\"telegraf\") |> window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundChronoUnit() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        -50L, ChronoUnit.WEEKS,
                        1L, ChronoUnit.SECONDS);

        String expected = "from(db:\"telegraf\") |> window(every: 15m, period: 20s, start: -50w, round: 1s)";
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundInstant() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        Instant.ofEpochSecond(1_750_000),
                        1L, ChronoUnit.SECONDS);

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z, round: 1s)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundChronoUnitColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        -50L, ChronoUnit.DAYS,
                        1L, ChronoUnit.HOURS,
                        "time", "superStart", "totalEnd");

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: -50d, round: 1h, column: \"time\", "
                + "startCol: \"superStart\", stopCol: \"totalEnd\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void windowEveryPeriodStartRoundInstantColumns() {

        Flux flux = Flux
                .from("telegraf")
                .window(15L, ChronoUnit.MINUTES,
                        20L, ChronoUnit.SECONDS,
                        Instant.ofEpochSecond(1_750_000),
                        1L, ChronoUnit.SECONDS,
                        "time", "superStart", "totalEnd");

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: 1970-01-21T06:06:40.000000000Z, round: 1s, column: \"time\", "
                + "startCol: \"superStart\", stopCol: \"totalEnd\")";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void namedParameters() {

        Flux flux = Flux
                .from("telegraf")
                .window()
                    .withPropertyNamed("every")
                    .withPropertyNamed("period")
                    .withPropertyNamed("start")
                    .withPropertyNamed("round");

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("every", new TimeInterval(15L, ChronoUnit.MINUTES));
        parameters.put("period", new TimeInterval(20L, ChronoUnit.SECONDS));
        parameters.put("start", new TimeInterval(-50L, ChronoUnit.DAYS));
        parameters.put("round", new TimeInterval(1L, ChronoUnit.HOURS));

        String expected = "from(db:\"telegraf\") |> "
                + "window(every: 15m, period: 20s, start: -50d, round: 1h)";

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters))).isEqualToIgnoringWhitespace(expected);
    }
}