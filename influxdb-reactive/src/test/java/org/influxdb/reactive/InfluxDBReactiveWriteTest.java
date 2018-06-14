package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 10:56)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveWriteTest extends AbstractInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {
        super.setUp(BatchOptionsReactive.DISABLED);
    }

    @Test
    void writePoint() {

        influxDBServer.enqueue(new MockResponse());

        Point point = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        // response
        Maybe<Point> pointMaybe = influxDBReactive.writePoint(point);
        pointMaybe.test()
                .assertSubscribed()
                .assertValue(point);

        // written point
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();

        assertThat(actual).isEqualTo(expected);

        // One request
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void writePointsIterable() {

        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());

        Point point1 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        Point point2 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 1.927)
                .addField("level description", "below 2 feet")
                .time(1440049800, TimeUnit.NANOSECONDS)
                .build();

        List<Point> points = new ArrayList<>();
        points.add(point1);
        points.add(point2);

        // response
        Flowable<Point> pointsFlowable = influxDBReactive.writePoints(points);
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        // Two requests
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(2);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void writePointsPublisher() {

        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());

        Point point1 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        Point point2 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 1.927)
                .addField("level description", "below 2 feet")
                .time(1440049800, TimeUnit.NANOSECONDS)
                .build();

        Point point3 = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 5.927)
                .addField("level description", "over 5 feet")
                .time(1440052800, TimeUnit.NANOSECONDS)
                .build();

        // response
        Flowable<Point> pointsFlowable = influxDBReactive.writePoints(Flowable.just(point1, point2, point3));
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2)
                .assertValueAt(2, point3);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        // Three requests
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(3);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void writeMeasurement() {

        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        // response
        Maybe<H2OFeetMeasurement> measurementMaybe = influxDBReactive.writeMeasurement(measurement);
        measurementMaybe.test()
                .assertSubscribed()
                .assertValue(measurement);

        // written measurement
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();

        assertThat(actual).isEqualTo(expected);

        // One request
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void writeMeasurementsIterable() {

        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        List<H2OFeetMeasurement> measurements = new ArrayList<>();
        measurements.add(measurement1);
        measurements.add(measurement2);

        // response
        Flowable<H2OFeetMeasurement> measurementsFlowable = influxDBReactive.writeMeasurements(measurements);
        measurementsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        // Two requests
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(2);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void writeMeasurementsPublisher() {

        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        H2OFeetMeasurement measurement3 = new H2OFeetMeasurement(
                "coyote_creek", 5.927, "over 5 feet", 1440052800L);

        // response
        Flowable<H2OFeetMeasurement> measurementsFlowable = influxDBReactive
                .writeMeasurements(Flowable.just(measurement1, measurement2, measurement3));

        measurementsFlowable
                .test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2)
                .assertValueAt(2, measurement3);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody();
        assertThat(actual).isEqualTo(expected);

        // Three requests
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(3);

        // there is no exception
        verifier.verifySuccess();
    }
}