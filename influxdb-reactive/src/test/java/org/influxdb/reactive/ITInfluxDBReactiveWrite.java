package org.influxdb.reactive;

import io.reactivex.BackpressureOverflowStrategy;
import io.reactivex.Flowable;
import io.reactivex.exceptions.MissingBackpressureException;
import io.reactivex.schedulers.TestScheduler;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.AbstractITInfluxDBReactiveTest;
import org.influxdb.impl.InfluxDBResultMapper;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jakub Bednar (bednar@github) (04/06/2018 10:06)
 */
@RunWith(JUnitPlatform.class)
class ITInfluxDBReactiveWrite extends AbstractITInfluxDBReactiveTest {

    @Test
    void write() {

        setUp(BatchOptionsReactive.DISABLED);

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046801L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049802L);

        // write
        influxDBReactive.writeMeasurements(Flowable.just(measurement1, measurement2));

        verifier.waitForResponse(2);

        // get from DB
        List<H2OFeetMeasurement> measurements = getMeasurements();
        assertThat(measurements.size()).isEqualTo(2);

        // measurement 1
        assertThat(measurements.get(0).getLocation()).isEqualTo("coyote_creek");
        assertThat(measurements.get(0).getLevel()).isEqualTo(2.927);
        assertThat(measurements.get(0).getDescription()).isEqualTo("below 3 feet");
        assertThat(measurements.get(0).getTime().toEpochMilli()).isEqualTo(1440046801L);

        // measurement 2
        assertThat(measurements.get(1).getLocation()).isEqualTo("coyote_creek");
        assertThat(measurements.get(1).getLevel()).isEqualTo(1.927);
        assertThat(measurements.get(1).getDescription()).isEqualTo("below 2 feet");
        assertThat(measurements.get(1).getTime().toEpochMilli()).isEqualTo(1440049802L);

        verifier.verifySuccess();
    }

    @Test
    void writeFail() {

        setUp(BatchOptionsReactive.DISABLED);

        Map<String, Object> fieldsToAdd = new HashMap<>();
        fieldsToAdd.put("level", null);

        Point point = Point.measurement("coyote_creek").fields(fieldsToAdd).build();

        // write
        influxDBReactive.writePoint(point);

        verifier.waitForResponse(1);

        verifier.verifyErrorResponse(1);
    }

    @Test
    void publishPattern() {

        setUp(BatchOptionsReactive.DISABLED);

        TestScheduler scheduler = new TestScheduler();

        // every 10 seconds publish new h2o level
        Flowable<H2OFeetMeasurement> observeWeather = Flowable.interval(10, TimeUnit.SECONDS, scheduler)
                .map(time -> {

                    double h2oLevel = time.doubleValue();
                    long timestamp = System.currentTimeMillis() + time;

                    return new H2OFeetMeasurement(
                            "coyote_creek", h2oLevel, "from ocean sensor", timestamp);
                });

        // write
        influxDBReactive.writeMeasurements(observeWeather);

        // 50 seconds to feature
        scheduler.advanceTimeBy(50, TimeUnit.SECONDS);

        verifier.waitForResponse(5);

        // get from DB
        List<H2OFeetMeasurement> measurements = getMeasurements();
        assertThat(measurements.size()).isEqualTo(5);

        assertThat(measurements.get(0).getLevel()).isEqualTo(0D);
        assertThat(measurements.get(1).getLevel()).isEqualTo(1D);
        assertThat(measurements.get(2).getLevel()).isEqualTo(2D);
        assertThat(measurements.get(3).getLevel()).isEqualTo(3D);
        assertThat(measurements.get(4).getLevel()).isEqualTo(4D);

        verifier.verifySuccess();
    }

    @Test
    void batchingOrderForJitter() {

        // after 5 actions or 10 seconds + 5 seconds jitter interval
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(2)
                .flushInterval(10_000)
                .jitterInterval(5_000)
                .build();

        setUp(batchOptions);

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek1", 0.927, "below 1 feet", null);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek2", 1.927, "below 2 feet", null);

        H2OFeetMeasurement measurement3 = new H2OFeetMeasurement(
                "coyote_creek3", 2.927, "below 3 feet", null);

        H2OFeetMeasurement measurement4 = new H2OFeetMeasurement(
                "coyote_creek4", 3.927, "below 4 feet", null);

        List<H2OFeetMeasurement> measurements1 = new ArrayList<>();
        measurements1.add(measurement1);
        measurements1.add(measurement2);

        List<H2OFeetMeasurement> measurements2 = new ArrayList<>();
        measurements2.add(measurement3);
        measurements2.add(measurement4);

        // publish measurement
        influxDBReactive.writeMeasurements(measurements1);
        influxDBReactive.writeMeasurements(measurements2);

        verifier.waitForResponse(2);

        // get from DB
        List<H2OFeetMeasurement> measurements = getMeasurements();
        assertThat(measurements.size()).isEqualTo(4);

        // measurement 1
        H2OFeetMeasurement measurement1DB = measurements.get(0);
        assertThat(measurement1DB.getLocation()).isEqualTo("coyote_creek1");
        assertThat(measurement1DB.getLevel()).isEqualTo(0.927);
        assertThat(measurement1DB.getDescription()).isEqualTo("below 1 feet");
        assertThat(measurement1DB.getTime().toEpochMilli()).isNotNull();

        // measurement 2
        H2OFeetMeasurement measurement2DB = measurements.get(1);
        assertThat(measurement2DB.getLocation()).isEqualTo("coyote_creek2");
        assertThat(measurement2DB.getLevel()).isEqualTo(1.927);
        assertThat(measurement2DB.getDescription()).isEqualTo("below 2 feet");
        assertThat(measurement2DB.getTime().toEpochMilli()).isNotNull();

        // measurement 3
        H2OFeetMeasurement measurement3DB = measurements.get(2);
        assertThat(measurement3DB.getLocation()).isEqualTo("coyote_creek3");
        assertThat(measurement3DB.getLevel()).isEqualTo(2.927);
        assertThat(measurement3DB.getDescription()).isEqualTo("below 3 feet");
        assertThat(measurement3DB.getTime().toEpochMilli()).isNotNull();

        // measurement 4
        H2OFeetMeasurement measurement4DB = measurements.get(3);
        assertThat(measurement4DB.getLocation()).isEqualTo("coyote_creek4");
        assertThat(measurement4DB.getLevel()).isEqualTo(3.927);
        assertThat(measurement4DB.getDescription()).isEqualTo("below 4 feet");
        assertThat(measurement4DB.getTime().toEpochMilli()).isNotNull();

        // same order as writes => timestamp in order
        Assertions.assertThat(measurement1DB.getTime()).isBeforeOrEqualTo(measurement2DB.getTime());
        Assertions.assertThat(measurement2DB.getTime()).isBefore(measurement3DB.getTime());
        Assertions.assertThat(measurement3DB.getTime()).isBeforeOrEqualTo(measurement4DB.getTime());

        verifier.verifySuccess();
    }

    @Test
    void backpressure() {

        setUp(BatchOptionsReactive.builder().build());

        Flowable<Point> map = Flowable
                .range(0, 20_000).map(index ->
                        Point.measurement("h2o_feet")
                                .tag("location", "coyote_creek" + index)
                                .addField("water_level", index)
                                .addField("level description", index + " feet")
                                .time(index, TimeUnit.NANOSECONDS)
                                .build());

        influxDBReactive.writePoints(map);
        influxDBReactive.close();

        // was backpressure
        Long backpressureCount = verifier
                .verifyBackpressure();

        // wait for response
        verifier.waitForWriteDisposed();

        // measurements + backpressure = 20 000
        List<H2OFeetMeasurement> measurements = getMeasurements();
        Assertions.assertThat(backpressureCount + measurements.size()).isEqualTo(20_000);

        verifier.verifySuccess();
    }

    @Test
    void backpressureErrorStrategy() {

        BatchOptionsReactive build = BatchOptionsReactive
                .builder()
                .backpressureStrategy(BackpressureOverflowStrategy.ERROR)
                .build();

        setUp(build);

        Flowable<Point> map = Flowable
                .range(0, 20_000).map(index ->
                        Point.measurement("h2o_feet")
                                .tag("location", "coyote_creek" + index)
                                .addField("water_level", index)
                                .addField("level description", index + " feet")
                                .time(index, TimeUnit.NANOSECONDS)
                                .build());

        influxDBReactive.writePoints(map);
        influxDBReactive.close();

        // wait for response
        verifier.waitForWriteDisposed();
        verifier.verifyError(0, MissingBackpressureException.class);
    }

    @Test
    void withoutBackpressure() {

        setUp(BatchOptionsReactive.builder().bufferLimit(20_000).build());

        Flowable<Point> map = Flowable
                .range(0, 20_000).map(index ->
                        Point.measurement("h2o_feet")
                                .tag("location", "coyote_creek" + index)
                                .addField("water_level", index)
                                .addField("level description", index + " feet")
                                .time(index, TimeUnit.NANOSECONDS)
                                .build());

        influxDBReactive.writePoints(map);
        influxDBReactive.close();

        // wait for response
        verifier.waitForWriteDisposed();

        // measurements + backpressure = 20 000
        List<H2OFeetMeasurement> measurements = getMeasurements();
        Assertions.assertThat(measurements.size()).isEqualTo(20_000);

        verifier.verifySuccess();
    }

    @Nonnull
    private List<H2OFeetMeasurement> getMeasurements() {

        QueryResult queryResult = influxDBCore
                .query(new Query("select * from h2o_feet", "reactive_database"));

        return new InfluxDBResultMapper().toPOJO(queryResult, H2OFeetMeasurement.class);
    }
}