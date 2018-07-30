package org.influxdb.flux.options.query;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.temporal.ChronoUnit;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 13:42)
 */
@RunWith(JUnitPlatform.class)
class TaskOptionTest {

    @Test
    void every() {

        TaskOption taskOption = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", every: 1h}");
    }

    @Test
    void delay() {

        TaskOption taskOption = TaskOption.builder("foo")
                .delay(10L, ChronoUnit.MINUTES)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", delay: 10m}");
    }

    @Test
    void cron() {

        TaskOption taskOption = TaskOption.builder("foo")
                .cron("0 2 * * *")
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", cron: \"0 2 * * *\"}");
    }

    @Test
    void retry() {

        TaskOption taskOption = TaskOption.builder("foo")
                .retry(5)
                .build();

        Assertions.assertThat(taskOption.toString()).isEqualToIgnoringWhitespace("option task = {name: \"foo\", retry: 5}");
    }

    @Test
    void full() {

        TaskOption taskOption = TaskOption.builder("foo")
                .every(1L, ChronoUnit.HOURS)
                .delay(10L, ChronoUnit.MINUTES)
                .cron("0 2 * * *")
                .retry(5)
                .build();

        Assertions.assertThat(taskOption.toString())
                .isEqualToIgnoringWhitespace("option task = {name: \"foo\", every: 1h, delay: 10m, cron: \"0 2 * * *\", retry: 5}");
    }
}