package org.influxdb.flux;

import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.flux.mapper.ColumnHeader;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.flux.mapper.Record;
import org.influxdb.flux.mapper.Table;
import org.influxdb.flux.operators.restriction.Restrictions;
import org.influxdb.impl.AbstractITFluxReactive;
import org.influxdb.reactive.events.WriteSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
    void oneToOneTable() {

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

        Flowable<FluxResult> results = fluxReactive.flux(flux);

        results
                .test()
                .assertValueCount(1)
                .assertValue(result -> {

                    Assertions.assertThat(result).isNotNull();

                    List<Table> tables = result.getTables();

                    Assertions.assertThat(tables).hasSize(1);

                    Table table = tables.get(0);
                    // Data types
                    Assertions.assertThat(table.getColumnHeaders()).hasSize(11);
                    Assertions.assertThat(table.getColumnHeaders().stream().map(ColumnHeader::getDataType))
                            .containsExactlyInAnyOrder("#datatype", "string", "long", "dateTime:RFC3339", "dateTime:RFC3339", "dateTime:RFC3339", "long", "string", "string", "string", "string");

                    // Columns
                    Assertions.assertThat(table.getColumnHeaders().stream().map(ColumnHeader::getColumnName))
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

                    Assertions.assertThat(record1.getValue()).isEqualTo(21L);

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

                    Assertions.assertThat(record2.getValue()).isEqualTo(42L);

                    Assertions.assertThat(record2.getTags()).hasSize(2);
                    Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");

                    return true;
                });
    }

    @Test
    @Disabled
    void oneToManyTable() {

        //
        // CURL
        //
        // curl -i -XPOST --data-urlencode 'q=from(db: "flux_database") |> range(start:0) |>
        // filter(fn:(r) => r._measurement == "mem" and r._field == "free") |> window(every:10s)'
        //  --data-urlencode "orgName=0" http://localhost:8093/v1/query

        // #datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string
        // #partition,false,false,true,true,false,false,true,true,true,true
        // #default,_result,,,,,,,,,
        // ,result,table,_start,_stop,_time,_value,_field,_measurement,host,region
        // ,,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west
        // ,,1,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west
        // ,,2,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,11,free,mem,A,west
        // ,,3,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,22,free,mem,B,west

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .window(10L, ChronoUnit.SECONDS);

        Flowable<FluxResult> results = fluxReactive.flux(flux);

        results
                .test()
                .assertValueCount(1)
                .assertValue(result -> {

                    Assertions.assertThat(result).isNotNull();

                    List<Table> tables = result.getTables();

                    Assertions.assertThat(tables).hasSize(4);

                    // Record1
                    Record record1 = tables.get(0).getRecords().get(0);
                    Assertions.assertThat(tables.get(0).getRecords()).hasSize(1);
                    Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record1.getField()).isEqualTo("free");

                    Assertions.assertThat(record1.getTags()).hasSize(2);
                    Assertions.assertThat(record1.getTags().get("host")).isEqualTo("A");
                    Assertions.assertThat(record1.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record1.getValue()).isEqualTo(10L);

                    // Record2
                    Record record2 = tables.get(1).getRecords().get(0);
                    Assertions.assertThat(tables.get(1).getRecords()).hasSize(1);
                    Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record2.getField()).isEqualTo("free");

                    Assertions.assertThat(record2.getTags()).hasSize(2);
                    Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record2.getValue()).isEqualTo(20L);

                    // Record3
                    Record record3 = tables.get(2).getRecords().get(0);
                    Assertions.assertThat(tables.get(2).getRecords()).hasSize(1);
                    Assertions.assertThat(record3.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record3.getField()).isEqualTo("free");

                    Assertions.assertThat(record3.getTags()).hasSize(2);
                    Assertions.assertThat(record3.getTags().get("host")).isEqualTo("A");
                    Assertions.assertThat(record3.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record3.getValue()).isEqualTo(11L);

                    // Record4
                    Record record4 = tables.get(3).getRecords().get(0);
                    Assertions.assertThat(tables.get(3).getRecords()).hasSize(1);
                    Assertions.assertThat(record4.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record4.getField()).isEqualTo("free");

                    Assertions.assertThat(record4.getTags()).hasSize(2);
                    Assertions.assertThat(record4.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record4.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record4.getValue()).isEqualTo(22L);

                    return true;
                });
    }

    @Test
    void manyToOne() {

        //
        // CURL
        //
        // curl -i -XPOST --data-urlencode 'q=from(db: "flux_database") |> range(start:0)
        // |> filter(fn:(r) => r._measurement == "mem" and r._field == "free") |> window(every:10s)
        // |> group(by:["region"])' --data-urlencode "orgName=0" http://localhost:8093/v1/query

        // #datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string
        // #partition,false,false,false,false,false,false,false,false,false,true
        // #default,_result,,,,,,,,,
        // ,result,table,_start,_stop,_time,_value,_field,_measurement,host,region
        // ,,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,10,free,mem,A,west
        // ,,0,1970-01-01T00:00:10Z,1970-01-01T00:00:20Z,1970-01-01T00:00:10Z,20,free,mem,B,west
        // ,,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,11,free,mem,A,west
        // ,,0,1970-01-01T00:00:20Z,1970-01-01T00:00:30Z,1970-01-01T00:00:20Z,22,free,mem,B,west

        Restrictions restriction = Restrictions
                .and(Restrictions.measurement().equal("mem"), Restrictions.field().equal("free"));

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .filter(restriction)
                .window(10L, ChronoUnit.SECONDS)
                .groupBy("region");

        Flowable<FluxResult> results = fluxReactive.flux(flux);

        results
                .test()
                .assertValueCount(1)
                .assertValue(result -> {

                    Assertions.assertThat(result).isNotNull();

                    List<Table> tables = result.getTables();

                    Assertions.assertThat(tables).hasSize(1);
                    Assertions.assertThat(tables.get(0).getRecords()).hasSize(4);

                    // Record1
                    Record record1 = tables.get(0).getRecords().get(0);
                    Assertions.assertThat(record1.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record1.getField()).isEqualTo("free");

                    Assertions.assertThat(record1.getTags()).hasSize(2);
                    Assertions.assertThat(record1.getTags().get("host")).isEqualTo("A");
                    Assertions.assertThat(record1.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record1.getValue()).isEqualTo(10L);

                    // Record2
                    Record record2 = tables.get(0).getRecords().get(1);
                    Assertions.assertThat(record2.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record2.getField()).isEqualTo("free");

                    Assertions.assertThat(record2.getTags()).hasSize(2);
                    Assertions.assertThat(record2.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record2.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record2.getValue()).isEqualTo(20L);

                    // Record3
                    Record record3 = tables.get(0).getRecords().get(2);
                    Assertions.assertThat(record3.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record3.getField()).isEqualTo("free");

                    Assertions.assertThat(record3.getTags()).hasSize(2);
                    Assertions.assertThat(record3.getTags().get("host")).isEqualTo("A");
                    Assertions.assertThat(record3.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record3.getValue()).isEqualTo(11L);

                    // Record4
                    Record record4 = tables.get(0).getRecords().get(3);
                    Assertions.assertThat(record4.getMeasurement()).isEqualTo("mem");
                    Assertions.assertThat(record4.getField()).isEqualTo("free");

                    Assertions.assertThat(record4.getTags()).hasSize(2);
                    Assertions.assertThat(record4.getTags().get("host")).isEqualTo("B");
                    Assertions.assertThat(record4.getTags().get("region")).isEqualTo("west");

                    Assertions.assertThat(record4.getValue()).isEqualTo(22L);

                    return true;
                });
    }
}
