package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.RequestBody;
import okio.Buffer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.annotation.Column;
import org.influxdb.annotation.Measurement;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:56)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveTest {

    private InfluxDBReactive influxDB;
    private ArgumentCaptor<RequestBody> requestBody;

    @BeforeEach
    void setUp() {

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://influxdb:8086")
                .username("admin")
                .password("password")
                .database("weather")
                .build();

        InfluxDBServiceReactive influxDBService = Mockito.mock(InfluxDBServiceReactive.class);
        influxDB = new InfluxDBReactiveImpl(options, influxDBService);

        requestBody = ArgumentCaptor.forClass(RequestBody.class);

        Mockito.doAnswer(invocation -> Single.just(Response.success(null))).when(influxDBService).writePointsReactive(
                Mockito.eq("admin"),
                Mockito.eq("password"),
                Mockito.eq("weather"),
                Mockito.eq("autogen"),
                Mockito.any(),
                Mockito.eq("one"),
                requestBody.capture());
    }

    @AfterEach
    void finish() {
        influxDB.close();
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

        // written point
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writePointsIterable() {

        Point point1 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level\\ description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        Point point2 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 1.927)
                .addField("level\\ description", "below 2 feet")
                .time(1440049800, TimeUnit.NANOSECONDS)
                .build();

        List<Point> points = new ArrayList<>();
        points.add(point1);
        points.add(point2);

        // response
        Flowable<Point> pointsFlowable = influxDB.writePoints(points);
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writePointsPublisher() {

        Point point1 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level\\ description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        Point point2 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 1.927)
                .addField("level\\ description", "below 2 feet")
                .time(1440049800, TimeUnit.NANOSECONDS)
                .build();

        Point point3 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 5.927)
                .addField("level\\ description", "over 5 feet")
                .time(1440052800, TimeUnit.NANOSECONDS)
                .build();

        // response
        Flowable<Point> pointsFlowable = influxDB.writePoints(Flowable.just(point1, point2, point3));
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2)
                .assertValueAt(2, point3);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody(requestBody, 2);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writeMeasurement() {

        H20Feet measurement = new H20Feet(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        // response
        Maybe<H20Feet> measurementMaybe = influxDB.writeMeasurement(measurement);
        measurementMaybe.test()
                .assertSubscribed()
                .assertValue(measurement);

        // written measurement
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writeMeasurementsIterable() {

        H20Feet measurement1 = new H20Feet(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H20Feet measurement2 = new H20Feet(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        List<H20Feet> measurements = new ArrayList<>();
        measurements.add(measurement1);
        measurements.add(measurement2);

        // response
        Flowable<H20Feet> measurementsFlowable = influxDB.writeMeasurements(measurements);
        measurementsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void writeMeasurementsPublisher() {

        H20Feet measurement1 = new H20Feet(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H20Feet measurement2 = new H20Feet(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        H20Feet measurement3 = new H20Feet(
                "coyote_creek", 5.927, "over 5 feet", 1440052800L);

        // response
        Flowable<H20Feet> measurementsFlowable = influxDB
                .writeMeasurements(Flowable.just(measurement1, measurement2, measurement3));

        measurementsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2)
                .assertValueAt(2, measurement3);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        Assertions.assertEquals(expected, actual);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody(requestBody, 2);
        Assertions.assertEquals(expected, actual);
    }

    @Nonnull
    private String pointsBody(@Nonnull final ArgumentCaptor<RequestBody> requestBody) {
        return pointsBody(requestBody, 0);
    }

    @Nonnull
    private String pointsBody(@Nonnull final ArgumentCaptor<RequestBody> requestBody,
                              @Nonnull final Integer captureValueIndex) {

        Assertions.assertNotNull(requestBody);

        Buffer sink = new Buffer();
        try {
            requestBody.getAllValues().get(captureValueIndex).writeTo(sink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sink.readUtf8();
    }

    @Measurement(name = "h2o_feet", timeUnit = TimeUnit.NANOSECONDS)
    public static class H20Feet {

        @Column(name = "location", tag = true)
        private String location;

        @Column(name = "water_level")
        private Double level;

        @Column(name = "level\\ description")
        private String description;

        @Column(name = "time")
        private Instant time;

        H20Feet(String location, Double level, String description, Long time) {
            this.location = location;
            this.level = level;
            this.description = description;
            this.time = Instant.ofEpochMilli(time);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof H20Feet)) return false;
            H20Feet h20Feet = (H20Feet) o;
            return Objects.equals(location, h20Feet.location) &&
                    Objects.equals(level, h20Feet.level) &&
                    Objects.equals(description, h20Feet.description) &&
                    Objects.equals(time, h20Feet.time);
        }

        @Override
        public int hashCode() {

            return Objects.hash(location, level, description, time);
        }
    }
}