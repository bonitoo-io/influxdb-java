package org.influxdb.reactive;

import io.reactivex.Maybe;
import org.influxdb.InfluxDB;
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
    private InfluxDBServiceReactive influxDBService;

    @BeforeEach
    void setUp() {

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://influxdb:8086")
                .username("admin")
                .password("password")
                .database("weather")
                .build();

        influxDBService = Mockito.mock(InfluxDBServiceReactive.class);
        influxDB = new InfluxDBReactiveImpl(options, influxDBService);
    }

    @Test
    void writePoint() {

        Point point = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level\\ description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        // response
        Maybe<Point> pointMaybe = influxDB.writePoint(point);
        pointMaybe.test()
                .assertSubscribed()
                .assertValue(point);

        // remote call
        Mockito.verify(influxDBService, Mockito.only()).writePointsReactive(
                Mockito.eq("admin"),
                Mockito.eq("password"),
                Mockito.eq("weather"),
                Mockito.eq("autogen"),
                Mockito.any(),
                Mockito.eq("one"),
                Mockito.any());
    }
}