package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.FluxChain;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;
import java.util.HashMap;

/**
 * @author Jakub Bednar (bednar@github) (17/07/2018 07:58)
 */
@RunWith(JUnitPlatform.class)
class MapFluxTest {

    @Test
    void map() {

        Restrictions restriction = Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_system"),
                Restrictions.tag("service").equal("app-server")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-12L, ChronoUnit.HOURS)
                .map("r._value * r._value");

        String expected = "from(db:\"telegraf\") "
                + "|> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\" AND r[\"service\"] == \"app-server\")) "
                + "|> range(start: -12h) "
                + "|> map(fn: (r) => r._value * r._value)";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void mapObject() {

        Restrictions restriction = Restrictions.and(
                Restrictions.measurement().equal("cpu"),
                Restrictions.field().equal("usage_system"),
                Restrictions.tag("service").equal("app-server")
        );

        Flux flux = Flux
                .from("telegraf")
                .filter(restriction)
                .range(-12L, ChronoUnit.HOURS)
                .map("{value: r._value, value2:r._value * r._value}");

        String expected = "from(db:\"telegraf\") "
                + "|> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_system\" AND r[\"service\"] == \"app-server\")) "
                + "|> range(start: -12h) "
                + "|> map(fn: (r) => {value: r._value, value2:r._value * r._value})";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void mapByParameter() {

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("function", "r._value * 10");

        Flux flux = Flux
                .from("telegraf")
                .range(-12L, ChronoUnit.HOURS)
                .map()
                    .withPropertyNamed("fn: (r)", "function");

        String expected = "from(db:\"telegraf\") "
                + "|> range(start: -12h) "
                + "|> map(fn: (r) => r._value * 10)";

        Assertions.assertThat(flux.print(new FluxChain().addParameters(parameters)))
                .isEqualToIgnoringWhitespace(expected);
    }
}