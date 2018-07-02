package org.influxdb.flux;

import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.Record;
import org.influxdb.flux.mapper.Table;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.influxdb.impl.AbstractITFluxReactive;
import org.influxdb.reactive.events.WriteSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 07:59)
 */
@DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
@RunWith(JUnitPlatform.class)
class ITFluxReactiveFlux extends AbstractITFluxReactive {

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

        LongAdder successEventCount = new LongAdder();
        influxDBReactive
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(writeSuccessEvent -> successEventCount.add(1));

        influxDBReactive.writePoints(Flowable.just(point1, point2, point3, point4, point5, point6));

        waitToSecondsTo(() -> successEventCount.intValue() != 6);
        waitToFlux();
    }

    @Test
    void inputTablesToOutputTable() {

        //
        // CURL
        //
        // curl -i -XPOST --data-urlencode 'q=from(db: "flux_database") |> range(start:0) |>
        // filter(fn:(r) => r._measurement == "mem" and r._field == "free") |> sum()'
        // --data-urlencode "orgName=0" http://localhost:8093/v1/query

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .sum();

        Flowable<FluxResult> results = fluxReactive.flux(flux).take(1);

        results
                .test()
                .assertValueCount(1)
                .assertValue(result -> {

                    Assertions.assertThat(result).isNotNull();

                    List<Table> tables = result.getTables();

                    Assertions.assertThat(tables).hasSize(1);

                    Table table = tables.get(0);
                    // Data types
                    Assertions.assertThat(table.getDataTypes()).hasSize(11);
                    Assertions.assertThat(table.getDataTypes())
                            .containsExactlyInAnyOrder("#datatype", "string","long","dateTime:RFC3339","dateTime:RFC3339","dateTime:RFC3339","long","string","string","string","string");

                    // Columns
                    Assertions.assertThat(table.getColumnNames()).hasSize(11);
                    Assertions.assertThat(table.getColumnNames())
                            .containsExactlyInAnyOrder("", "result", "table", "_start", "_stop", "_time", "_value", "_field", "_measurement", "host", "region");

                    // Records
                    Assertions.assertThat(table.getRecords()).hasSize(2);

                    // Record 1
                    Record record1 = table.getRecords().get(0);
                    Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record1.getField()).isEqualTo("free");

                    Assertions.assertThat(record1.getStart()).isEqualTo(Instant.EPOCH);
                    Assertions.assertThat(record1.getStop()).isNotNull();
                    Assertions.assertThat(record1.getTime()).isEqualTo(Instant.ofEpochSecond(10));

                    Assertions.assertThat(record1.getValue()).isEqualTo("21");

                    Assertions.assertThat(record1.getTags()).hasSize(2);
                    Assertions.assertThat(record1.getTags().get("host")).isEqualTo("A");
                    Assertions.assertThat(record1.getTags().get("region")).isEqualTo("west");

                    // Record 2
                    Record record2 = table.getRecords().get(1);
                    Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record2.getField()).isEqualTo("free");

                    Assertions.assertThat(record2.getStart()).isEqualTo(Instant.EPOCH);
                    Assertions.assertThat(record2.getStop()).isNotNull();
                    Assertions.assertThat(record2.getTime()).isEqualTo(Instant.ofEpochSecond(10));

                    Assertions.assertThat(record2.getValue()).isEqualTo("42");

                    Assertions.assertThat(record2.getTags()).hasSize(2);
                    Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");

                    return true;
                });
    }
}
