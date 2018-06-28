package org.influxdb.flux;

import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.flux.mapper.FluxResult;
import org.influxdb.impl.AbstractITFluxReactive;
import org.influxdb.reactive.events.WriteSuccessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Jakub Bednar (bednar@github) (28/06/2018 07:59)
 */
@DisabledIfSystemProperty(named = "FLUX_DISABLE", matches = "true")
class ITFluxReactiveFlux extends AbstractITFluxReactive {

    @BeforeEach
    void prepareDate() throws InterruptedException {
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

        Flux flux = Flux.from(DATABASE_NAME)
                .range(Instant.EPOCH)
                .expression("filter(fn:(r) => r._measurement == \"mem\" and r._field == \"free\")")
                .sum();

        Flowable<FluxResult> results = fluxReactive.flux(flux).take(1);

        results.take(1)
                .test()
                .assertValueCount(1)
                .assertValue(result -> {

                    Assertions.assertThat(result).isNotNull();

                    String[] content = result.getContent().split("\n");

                    // Data types
                    Assertions.assertThat(content[0])
                            .isEqualToIgnoringWhitespace("#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,long,string,string,string,string");

                    // Partition
                    Assertions.assertThat(content[1])
                            .isEqualToIgnoringWhitespace("#partition,false,false,true,true,false,false,true,true,true,true");
                    // Default
                    Assertions.assertThat(content[2])
                            .isEqualToIgnoringWhitespace("#default,_result,,,,,,,,,");

                    // Columns
                    Assertions.assertThat(content[3])
                            .isEqualToIgnoringWhitespace(",result,table,_start,_stop,_time,_value,_field,_measurement,host,region");

                    // Row 1
                    Assertions.assertThat(content[4]).contains(",1970-01-01T00:00:10Z,21,free,mem,A,west");

                    // Row 2
                    Assertions.assertThat(content[5]).contains(",1970-01-01T00:00:10Z,42,free,mem,B,west");

                    return true;
                });
    }
}
