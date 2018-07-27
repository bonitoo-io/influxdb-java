package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.influxdb.flux.options.query.AbstractOption;
import org.influxdb.flux.options.query.NowOption;
import org.influxdb.flux.options.query.TaskOption;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:27)
 */
@RunWith(JUnitPlatform.class)
class FluxTest {

    @Test
    void flux1() {

        String flux = Flux
                .from("telegraf")
                .count()
                .print();

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("from(db:\"telegraf\") |> count()");
    }

    @Test
    void fluxWithOneOptions() {

        NowOption now = NowOption.builder()
                .function("giveMeTime()")
                .build();

        List<AbstractOption> options = new ArrayList<>();
        options.add(now);

        String flux = Flux
                .from("telegraf")
                .count()
                .print(new FluxChain().addOptions(options));

        Assertions.assertThat(flux).isEqualToIgnoringWhitespace("option now = giveMeTime() from(db:\"telegraf\") |> count()");
    }

    @Test
    void fluxWithTwoOptions() {

        NowOption now = NowOption.builder()
                .function("giveMeTime()")
                .build();

        TaskOption task = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .delay(10L, ChronoUnit.MINUTES)
                .cron("0 2 * * *")
                .retry(5)
                .build();

        List<AbstractOption> options = new ArrayList<>();
        options.add(now);
        options.add(task);

        String flux = Flux
                .from("telegraf")
                .count()
                .print(new FluxChain().addOptions(options));

        String expected = "option now = giveMeTime() "
                + "option task = {name: \"foo\", every: 1h, delay: 10m, cron: \"0 2 * * *\", retry: 5} "
                + "from(db:\"telegraf\") |> count()";
        
        Assertions.assertThat(flux).isEqualToIgnoringWhitespace(expected);
    }
}