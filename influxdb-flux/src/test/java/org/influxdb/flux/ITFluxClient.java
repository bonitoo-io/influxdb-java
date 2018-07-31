package org.influxdb.flux;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.influxdb.dto.Point;
import org.influxdb.flux.mapper.ColumnHeader;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.Record;
import org.influxdb.flux.mapper.Table;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.influxdb.impl.AbstractITFluxClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 09:30)
 */
@DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
@RunWith(JUnitPlatform.class)
class ITFluxClient extends AbstractITFluxClient {

    @BeforeEach
    void prepareDate() {
        Point point1 = Point.measurement("mem")
                .tag("host", "A").tag("region", "west")
                .addField("free", 10)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point2 = Point.measurement("mem")
                .tag("host", "A").tag("region", "west")
                .addField("free", 11)
                .time(20, TimeUnit.SECONDS)
                .build();

        Point point3 = Point.measurement("mem")
                .tag("host", "B").tag("region", "west")
                .addField("free", 20)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point4 = Point.measurement("mem")
                .tag("host", "B").tag("region", "west")
                .addField("free", 22)
                .time(20, TimeUnit.SECONDS)
                .build();

        Point point5 = Point.measurement("cpu")
                .tag("host", "A").tag("region", "west")
                .addField("user_usage", 45)
                .addField("usage_system", 35)
                .time(10, TimeUnit.SECONDS)
                .build();
        Point point6 = Point.measurement("cpu")
                .tag("host", "A").tag("region", "west")
                .addField("user_usage", 49)
                .addField("usage_system", 38)
                .time(20, TimeUnit.SECONDS)
                .build();

        influxDB.write(point1);
        influxDB.write(point2);
        influxDB.write(point3);
        influxDB.write(point4);
        influxDB.write(point5);
        influxDB.write(point6);
    }

    @Test
    void query() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        FluxResult fluxResult = fluxClient.flux(flux);

        assertFluxResult(fluxResult);
    }

    @Test
    void callback() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        fluxClient.flux(flux, fluxResult -> {

            assertFluxResult(fluxResult);

            countDownLatch.countDown();
        });

        waitToCallback();
    }

    @Test
    void queryToPOJO() {

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .window(10L, ChronoUnit.SECONDS)
                .groupBy("region");

        List<Memory> memories = fluxClient.flux(flux, Memory.class);

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

    @Test
    void ping() {

        Assertions.assertThat(fluxClient.ping()).isTrue();
    }

    private void assertFluxResult(@Nonnull final FluxResult fluxResult) {

        Assertions.assertThat(fluxResult).isNotNull();

        List<Table> tables = fluxResult.getTables();

        Assertions.assertThat(tables).hasSize(2);

        Table table1 = tables.get(0);
        // Data types
        Assertions.assertThat(table1.getColumnHeaders()).hasSize(11);
        Assertions.assertThat(table1.getColumnHeaders().stream().map(ColumnHeader::getDataType))
                .containsExactlyInAnyOrder("#datatype", "string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

        // Columns
        Assertions.assertThat(table1.getColumnHeaders().stream().map(ColumnHeader::getColumnName))
                .containsExactlyInAnyOrder("", "result", "table", "_start", "_stop", "_time", "_value", "_field", "_measurement", "host", "region");

        // Records
        Assertions.assertThat(table1.getRecords()).hasSize(1);

        // Record 1
        Record record1 = table1.getRecords().get(0);
        Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record1.getField()).isEqualTo("free");

        Assertions.assertThat(record1.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record1.getStop()).isNotNull();
        Assertions.assertThat(record1.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record1.getValue()).isEqualTo(21L);

        Assertions.assertThat(record1.getTags()).hasSize(2);
        Assertions.assertThat(record1.getTags().get("host")).isEqualTo("A");
        Assertions.assertThat(record1.getTags().get("region")).isEqualTo("west");

        // Record 2
        Record record2 = tables.get(1).getRecords().get(0);
        Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
        Assertions.assertThat(record2.getField()).isEqualTo("free");

        Assertions.assertThat(record2.getStart()).isEqualTo(Instant.EPOCH);
        Assertions.assertThat(record2.getStop()).isNotNull();
        Assertions.assertThat(record2.getTime()).isEqualTo(Instant.ofEpochSecond(10));

        Assertions.assertThat(record2.getValue()).isEqualTo(42L);

        Assertions.assertThat(record2.getTags()).hasSize(2);
        Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
        Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");
    }
}