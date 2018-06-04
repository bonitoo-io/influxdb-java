package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import okhttp3.RequestBody;
import okio.Buffer;
import org.influxdb.InfluxDBOptions;
import org.influxdb.dto.Point;
import org.influxdb.impl.InfluxDBServiceReactive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import retrofit2.Response;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


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

        Mockito.doAnswer(invocation -> Single.just(Response.success(null))).when(influxDBService).writePoints(
                Mockito.eq("admin"),
                Mockito.eq("password"),
                Mockito.eq("weather"),
                Mockito.eq("autogen"),
                Mockito.any(),
                Mockito.eq("one"),
                requestBody.capture());
    }

    @AfterEach
    void cleanUp() {
        influxDB.close();
    }

    @Test
    void writePoint() {

        Point point = Point.measurement("h2o_feet")
                .tag("location", "coyote_creek")
                .addField("water_level", 2.927)
                .addField("level description", "below 3 feet")
                .time(1440046800, TimeUnit.NANOSECONDS)
                .build();

        // response
        Maybe<Point> pointMaybe = influxDB.writePoint(point);
        pointMaybe.test()
                .assertSubscribed()
                .assertValue(point);

        // written point
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writePointsIterable() {

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
        Flowable<Point> pointsFlowable = influxDB.writePoints(points);
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writePointsPublisher() {

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
        Flowable<Point> pointsFlowable = influxDB.writePoints(Flowable.just(point1, point2, point3));
        pointsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, point1)
                .assertValueAt(1, point2)
                .assertValueAt(2, point3);

        // written points
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody(requestBody, 2);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeMeasurement() {

        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        // response
        Maybe<H2OFeetMeasurement> measurementMaybe = influxDB.writeMeasurement(measurement);
        measurementMaybe.test()
                .assertSubscribed()
                .assertValue(measurement);

        // written measurement
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeMeasurementsIterable() {

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        List<H2OFeetMeasurement> measurements = new ArrayList<>();
        measurements.add(measurement1);
        measurements.add(measurement2);

        // response
        Flowable<H2OFeetMeasurement> measurementsFlowable = influxDB.writeMeasurements(measurements);
        measurementsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void writeMeasurementsPublisher() {

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049800L);

        H2OFeetMeasurement measurement3 = new H2OFeetMeasurement(
                "coyote_creek", 5.927, "over 5 feet", 1440052800L);

        // response
        Flowable<H2OFeetMeasurement> measurementsFlowable = influxDB
                .writeMeasurements(Flowable.just(measurement1, measurement2, measurement3));

        measurementsFlowable.test()
                .assertSubscribed()
                .assertValueAt(0, measurement1)
                .assertValueAt(1, measurement2)
                .assertValueAt(2, measurement3);

        // written measurements
        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        String actual = pointsBody(requestBody, 0);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049800";
        actual = pointsBody(requestBody, 1);
        assertThat(actual).isEqualTo(expected);

        expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"over 5 feet\",water_level=5.927 1440052800";
        actual = pointsBody(requestBody, 2);
        assertThat(actual).isEqualTo(expected);
    }

    @Nonnull
    private String pointsBody(@Nonnull final ArgumentCaptor<RequestBody> requestBody) {
        return pointsBody(requestBody, 0);
    }

    @Nonnull
    private String pointsBody(@Nonnull final ArgumentCaptor<RequestBody> requestBody,
                              @Nonnull final Integer captureValueIndex) {

        Objects.requireNonNull(requestBody, "RequestBody is required");
        Objects.requireNonNull(captureValueIndex, "CaptureValueIndex is required");

        Buffer sink = new Buffer();
        try {
            requestBody.getAllValues().get(captureValueIndex).writeTo(sink);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sink.readUtf8();
    }
}