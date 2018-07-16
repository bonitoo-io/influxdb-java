package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Predicate;
import io.reactivex.subscribers.TestSubscriber;
import org.assertj.core.api.Assertions;
import org.influxdb.dto.BoundParameterQuery;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.impl.AbstractITInfluxDBReactiveTest;
import org.influxdb.reactive.options.BatchOptionsReactive;
import org.influxdb.reactive.options.QueryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Jakub Bednar (bednar@github) (11/06/2018 11:33)
 */
@RunWith(JUnitPlatform.class)
class ITInfluxDBReactiveQuery extends AbstractITInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {

        super.setUp(BatchOptionsReactive.DEFAULTS);

        Flowable<H2OFeetMeasurement> measurements = Flowable.range(0, 1000).map(index -> {

            String location = getLocation(index);
            double level = index.doubleValue();
            long millis = TimeUnit.MILLISECONDS.convert(index, TimeUnit.SECONDS);

            return new H2OFeetMeasurement(location, level, "Feet = " + index, millis);
        });

        influxDBReactive.writeMeasurements(measurements);
        verifier.waitForResponse(1);
    }

    @Test
    void chunked() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);
        QueryOptions options = QueryOptions.builder().chunkSize(1).build();

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class, options);

        measurements
                .take(5)
                .test()
                .assertValueCount(5)
                .assertValueAt(0, assertMeasurement(0))
                .assertValueAt(1, assertMeasurement(1))
                .assertValueAt(2, assertMeasurement(2))
                .assertValueAt(3, assertMeasurement(3))
                .assertValueAt(4, assertMeasurement(4));

        verifier.verifyResponseMapperCalls(5);
    }

    @Test
    void order() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);
        QueryOptions options = QueryOptions.builder().chunkSize(1).build();

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class, options);

        TestSubscriber<H2OFeetMeasurement> testSubscriber = measurements
                .test()
                .assertValueCount(1000);

        for (int i = 0; i < 1000; i++) {
            testSubscriber
                    .assertValueAt(i, assertMeasurement(i));
        }
    }

    @Test
    void defaultChunking() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class);

        measurements
                .take(5)
                .test()
                .assertValueCount(5)
                .assertValueAt(0, assertMeasurement(0))
                .assertValueAt(1, assertMeasurement(1))
                .assertValueAt(2, assertMeasurement(2))
                .assertValueAt(3, assertMeasurement(3))
                .assertValueAt(4, assertMeasurement(4));

        verifier.verifyResponseMapperCalls(1);
    }

    @Test
    void filtering() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);
        QueryOptions options = QueryOptions.builder().chunkSize(1).build();

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class, options);

        Single<Long> santaMonicaRecords = measurements
                .filter(measurement -> measurement.getLocation().equals("santa_monica"))
                .count();

        santaMonicaRecords
                .test()
                .assertValue(500L);

        verifier.verifyResponseMapperCalls(1000);
    }

    @Test
    void overResultsCount() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class);

        measurements
                .take(5_000)
                .test()
                .assertValueCount(1_000);

        verifier.verifyResponseMapperCalls(1);
    }

    @Test
    void supportBoundQuery() {

        BoundParameterQuery query = BoundParameterQuery.QueryBuilder
                .newQuery("select * from h2o_feet where location = $location")
                .forDatabase(DATABASE_NAME)
                .bind("location", "coyote_creek")
                .create();

        Single<Long> coyoteCreekRecords = influxDBReactive.query(query, H2OFeetMeasurement.class)
                .count();

        coyoteCreekRecords
                .test()
                .assertValue(500L);

        verifier.verifyResponseMapperCalls(1);
    }

    @Test
    void useDatabaseFromQuery() {

        simpleQuery("CREATE DATABASE europe_reactive_database");

        Query query = new Query("select * from h2o_feet", "europe_reactive_database");

        Flowable<H2OFeetMeasurement> measurements = influxDBReactive.query(query, H2OFeetMeasurement.class);
        measurements
                .test()
                .assertValueCount(0);
    }

    @Test
    void timeUnit() {

        Query query = new Query("select * from h2o_feet", DATABASE_NAME);

        influxDBReactive.query(query, QueryOptions.builder().precision(TimeUnit.MILLISECONDS).build())
                .test()
                .assertValueCount(1)
                .assertValue(createPredicate(TimeUnit.MILLISECONDS));

        influxDBReactive.query(query, QueryOptions.builder().precision(TimeUnit.SECONDS).build())
                .test()
                .assertValueCount(1)
                .assertValue(createPredicate(TimeUnit.SECONDS));

        influxDBReactive.query(query, QueryOptions.builder().precision(TimeUnit.NANOSECONDS).build())
                .test()
                .assertValueCount(1)
                .assertValue(createPredicate(TimeUnit.NANOSECONDS));
    }

    @Nonnull
    private Predicate<QueryResult> createPredicate(@Nonnull final TimeUnit requiredUnit) {

        return result -> {

            List<List<Object>> values = result.getResults().get(0).getSeries().get(0).getValues();

            List<Long> times = values.stream()
                    .map(objects -> ((Double) objects.get(0)).longValue())
                    .collect(Collectors.toList());

            for (int i = 0; i < 999; i++) {
                Assertions.assertThat(times.get(i)).isEqualTo(requiredUnit.convert(i, TimeUnit.SECONDS));
            }

            return true;
        };
    }

    @Nonnull
    private String getLocation(@Nonnull final Integer index) {
        String location;
        if (index % 2 == 0) {
            location = "coyote_creek";
        } else {
            location = "santa_monica";
        }
        return location;
    }

    @Nonnull
    private Predicate<H2OFeetMeasurement> assertMeasurement(final int index) {

        return measurement -> {

            long millis = TimeUnit.MILLISECONDS.convert(index, TimeUnit.SECONDS);
            
            Assertions.assertThat(measurement.getLocation()).isEqualTo(getLocation(index));
            Assertions.assertThat(measurement.getLevel()).isEqualTo(index);
            Assertions.assertThat(measurement.getDescription()).isEqualTo("Feet = " + index);
            Assertions.assertThat(measurement.getTime()).isEqualTo(Instant.ofEpochMilli(millis));

            return true;
        };
    }
}