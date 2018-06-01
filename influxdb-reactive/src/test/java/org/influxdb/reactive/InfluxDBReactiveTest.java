package org.influxdb.reactive;

import io.reactivex.Maybe;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:56)
 */
@Disabled
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveTest {

    private InfluxDBReactive influxDB;

    @BeforeEach
    void setUp() {
        InfluxDBOptions options = InfluxDBOptions.builder().url("http://influxdb:8086").build();

        influxDB = new InfluxDBReactiveImpl(options, Mockito.mock(InfluxDBServiceReactive.class));
    }

    @Test
    void writePoint() {

        Point point = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level\\ description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        Maybe<Point> pointMaybe = influxDB.writePoint(point);

        pointMaybe.test().assertSubscribed().assertValue(point);
    }
}