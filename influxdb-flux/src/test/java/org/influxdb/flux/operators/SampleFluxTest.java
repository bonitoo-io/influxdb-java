package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;

import static org.influxdb.flux.operators.restriction.Restrictions.and;
import static org.influxdb.flux.operators.restriction.Restrictions.field;
import static org.influxdb.flux.operators.restriction.Restrictions.measurement;

/**
 * @author Jakub Bednar (bednar@github) (29/06/2018 07:37)
 */
@RunWith(JUnitPlatform.class)
class SampleFluxTest {

    @Test
    void sampleN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS)
                .sample(10);

        String expected = "from(db:\"telegraf\") |> "
                + "filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\")) |> "
                + "range(start: -1d) |> sample(n: 10)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void sampleNPosition() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS)
                .sample(5, 1);

        String expected = "from(db:\"telegraf\") |> "
                + "filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\")) |> "
                + "range(start: -1d) |> sample(n: 5, pos: 1)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void samplePositionGreaterThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);


        Assertions.assertThatThrownBy(() -> flux.sample(5, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }

    @Test
    void samplePositionSameThanN() {

        Flux flux = Flux.from("telegraf")
                .filter(and(measurement().equal("cpu"), field().equal("usage_system")))
                .range(-1L, ChronoUnit.DAYS);

        Assertions.assertThatThrownBy(() -> flux.sample(10, 10))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("pos must be less than n");
    }
}