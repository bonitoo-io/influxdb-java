package org.influxdb.flux.operators;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.Flux;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (19/07/2018 12:59)
 */
@RunWith(JUnitPlatform.class)
class JoinFluxText {

    @Test
    void join() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux flux = Flux
                .join("cpu", cpu, "mem", mem, "host", "tables.cpu[\"_value\"] + tables.mem[\"_value\"]");

        String expected = "cpu = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\"], fn: (tables) => tables.cpu[\"_value\"] + tables.mem[\"_value\"])";
        
        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }

    @Test
    void joinByParameters() {

        Flux cpu = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("cpu"), Restrictions.field().equal("usage_user")))
                .range(-30L, ChronoUnit.MINUTES);

        Flux mem = Flux.from("telegraf")
                .filter(Restrictions.and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("used_percent")))
                .range(-30L, ChronoUnit.MINUTES);

        List<String> tags = new ArrayList<>();
        tags.add("host");
        tags.add("production");

        Flux flux = Flux.join()
                .withTable("cpu", cpu)
                .withTable("mem", mem)
                .withOn(tags)
                .withFunction("tables.cpu[\"_value\"] + tables.mem[\"_value\"]");

        String expected = "cpu = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" AND r[\"_field\"] == \"usage_user\")) |> range(start: -30m) "
                + "mem = from(db:\"telegraf\") |> filter(fn: (r) => (r[\"_measurement\"] == \"mem\" AND r[\"_field\"] == \"used_percent\")) |> range(start: -30m) "
                + "join(tables: {cpu:cpu, mem:mem}, on: [\"host\", \"production\"], fn: (tables) => tables.cpu[\"_value\"] + tables.mem[\"_value\"])";

        Assertions.assertThat(flux.print()).isEqualToIgnoringWhitespace(expected);
    }
}