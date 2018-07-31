package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.impl.AbstractFluxClientTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 07:05)
 */
@RunWith(JUnitPlatform.class)
class FluxClientQueryTest extends AbstractFluxClientTest {

    @Test
    void query() {

        fluxServer.enqueue(createResponse());

        FluxResult result = fluxClient.flux(Flux.from("flux_database"));

        assertSuccessResult(result);
    }

    @Test
    void queryCallback() {

        fluxServer.enqueue(createResponse());

        fluxClient.flux(Flux.from("flux_database"), result -> {
            assertSuccessResult(result);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void mapToPOJO() {

        fluxServer.enqueue(createResponse());

        List<Memory> memories = fluxClient.flux(Flux.from("flux_database"), Memory.class);

        assertSuccessPOJOs(memories);
    }

    @Test
    void mapToPOJOCallback() {

        fluxServer.enqueue(createResponse());

        fluxClient.flux(Flux.from("flux_database"), Memory.class, memories -> {
            assertSuccessPOJOs(memories);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    private void assertSuccessResult(@Nonnull final FluxResult result) {

        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result.getTables()).hasSize(1);
        Assertions.assertThat(result.getTables().get(0).getRecords()).hasSize(4);
    }

    private void assertSuccessPOJOs(@Nonnull final List<Memory> memories) {

        Assertions.assertThat(memories).hasSize(4);

        // Memory 1
        Assertions.assertThat(memories.get(0)).is(new Condition<>(memory -> {

            Assertions.assertThat(memory).isNotNull();
            Assertions.assertThat(memory.time).isEqualTo(Instant.ofEpochSecond(10));
            Assertions.assertThat(memory.free).isEqualTo(10L);
            Assertions.assertThat(memory.host).isEqualTo("A");
            Assertions.assertThat(memory.region).isEqualTo("west");

            return true;
        }, null));

        // Memory 2
        Assertions.assertThat(memories.get(1)).is(new Condition<>(memory -> {

            Assertions.assertThat(memory).isNotNull();
            Assertions.assertThat(memory.time).isEqualTo(Instant.ofEpochSecond(10));
            Assertions.assertThat(memory.free).isEqualTo(20L);
            Assertions.assertThat(memory.host).isEqualTo("B");
            Assertions.assertThat(memory.region).isEqualTo("west");

            return true;
        }, null));

        // Memory 3
        Assertions.assertThat(memories.get(2)).is(new Condition<>(memory -> {

            Assertions.assertThat(memory).isNotNull();
            Assertions.assertThat(memory.time).isEqualTo(Instant.ofEpochSecond(20));
            Assertions.assertThat(memory.free).isEqualTo(11L);
            Assertions.assertThat(memory.host).isEqualTo("A");
            Assertions.assertThat(memory.region).isEqualTo("west");

            return true;
        }, null));

        // Memory 4
        Assertions.assertThat(memories.get(3)).is(new Condition<>(memory -> {

            Assertions.assertThat(memory).isNotNull();
            Assertions.assertThat(memory.time).isEqualTo(Instant.ofEpochSecond(20));
            Assertions.assertThat(memory.free).isEqualTo(22L);
            Assertions.assertThat(memory.host).isEqualTo("B");
            Assertions.assertThat(memory.region).isEqualTo("west");

            return true;
        }, null));
    }

    @Measurement(name = "mem")
    public static class Memory {

        @Column(name = "time")
        private Instant time;

        @Column(name = "free")
        private Long free;

        @Column(name = "host", tag = true)
        private String host;

        @Column(name = "region", tag = true)
        private String region;
    }
}