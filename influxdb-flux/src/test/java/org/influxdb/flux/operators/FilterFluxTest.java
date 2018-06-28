package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 11:46)
 */
@RunWith(JUnitPlatform.class)
class FilterFluxTest {

    @Test
    void filter() {

        Restrictions restriction = Restrictions.and(
                Restrictions.tag("t1").equal("val1"),
                Restrictions.tag("t2").equal("val2")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, -2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(db:\"telegraf\") |> filter(fn: (r) => (r[\"t1\"]==\"val1\" AND r[\"t2\"]==\"val2\")) |> "
                + "range(start:-4h, stop:-2h) |> count()";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterExample() {

        Restrictions restriction = Restrictions.and(
                Restrictions.measurement().equal("mem"),
                Restrictions.field().equal("usage_system"),
                Restrictions.tag("service").equal("app-server")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, ChronoUnit.HOURS)
                .count();

        String expected = "from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"usage_system\" AND r[\"service\"] == \"app-server\")) |> "
                + "range(start:-4h) |> count()";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterInnerOr() {

        Restrictions and = Restrictions.and(
                Restrictions.tag("t1").equal("val1"),
                Restrictions.tag("t2").equal("val2")
        );

        Restrictions restrictions = Restrictions.or(and, Restrictions.tag("t3").equal("val3"));

        Flux flux = Flux
                .from("telegraf")
                .filter(restrictions)
                .range(4L, 2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(db:\"telegraf\") |> filter(fn: (r) => ((r[\"t1\"]==\"val1\" AND r[\"t2\"]==\"val2\") OR r[\"t3\"]==\"val3\")) |> "
                + "range(start:4h, stop:2h) |> count()";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void filterDoubleAndRegexpValue() {

        Restrictions restriction = Restrictions.and(
                Restrictions.tag("t1").equal(Pattern.compile("/val1/")),
                Restrictions.field().equal(10.5D)
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-4L, 2L, ChronoUnit.HOURS)
                .count();

        String expected = "from(db:\"telegraf\") |> filter(fn: (r) => (r[\"t1\"]==/val1/ AND r[\"_field\"] == 10.5)) |> "
                + "range(start:-4h, stop:2h) |> count()";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

}