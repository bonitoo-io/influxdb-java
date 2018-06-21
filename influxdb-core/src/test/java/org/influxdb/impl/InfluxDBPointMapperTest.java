package org.influxdb.impl;

import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (21/06/2018 08:17)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBPointMapperTest extends AbstractMapperTest {

    private InfluxDBPointMapper mapper = new InfluxDBPointMapper();

    @Test
    void mapMeasurement() {

        MyCustomMeasurement measurement = createMeasurement();

        Point point = mapper.toPoint(measurement, TimeUnit.MILLISECONDS);

        Assertions.assertThat(point).isNotNull();

        String expected = "CustomMeasurement booleanObject=false,booleanPrimitive=true,doubleObject=1.01,"
                + "doublePrimitive=4.0,integerObject=3i,integerPrimitive=4i,longObject=2i,"
                + "longPrimitive=3i,uuid=\"677e5d77-19b0-4dd7-9afc-3e826c922642\" 1500000000";

        Assertions.assertThat(point.lineProtocol(TimeUnit.MILLISECONDS)).isEqualTo(expected);
    }

    @Test
    void mapMeasurementCustomPrecision() {

        MyCustomMeasurement measurement = createMeasurement();

        Point point = mapper.toPoint(measurement, TimeUnit.SECONDS);

        Assertions.assertThat(point).isNotNull();

        String expected = "CustomMeasurement booleanObject=false,booleanPrimitive=true,doubleObject=1.01,"
                + "doublePrimitive=4.0,integerObject=3i,integerPrimitive=4i,longObject=2i,"
                + "longPrimitive=3i,uuid=\"677e5d77-19b0-4dd7-9afc-3e826c922642\" 1500000";

        Assertions.assertThat(point.lineProtocol(TimeUnit.SECONDS)).isEqualTo(expected);
    }

    @Nonnull
    private MyCustomMeasurement createMeasurement() {

        MyCustomMeasurement measurement = new MyCustomMeasurement();
        measurement.time = Instant.ofEpochMilli(1_500_000_000);
        measurement.uuid = "677e5d77-19b0-4dd7-9afc-3e826c922642";
        measurement.doubleObject = Double.valueOf("1.01");
        measurement.longObject = Long.valueOf("2");
        measurement.integerObject = Integer.valueOf("3");
        measurement.doublePrimitive = 4D;
        measurement.longPrimitive = 3L;
        measurement.integerPrimitive = 4;
        measurement.booleanObject = Boolean.valueOf("false");
        measurement.booleanPrimitive = true;

        return measurement;
    }
}