package org.influxdb.reactive;

import io.reactivex.Flowable;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveBatchingTest extends AbstractInfluxDBReactiveTest {

    @Test
    void flushByActions() {

        // after 5 actions
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled().actions(5).flushInterval(1_000_000).build();
        setUp(batchOptions);

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDBReactive.writeMeasurements(measurements);

        // only 4 actions
        Mockito.verify(influxDBService, Mockito.never()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // the fifth action => store to InfluxDB
        influxDBReactive.writeMeasurement(createMeasurement(5));

        // was call remote API
        Mockito.verify(influxDBService, Mockito.only()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // check content
        Assertions.assertThat(requestBody.getAllValues().size()).isEqualTo(1);

        String expectedContent = Stream.of
                ("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 1440046802",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 1440046803",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 1440046804",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 5\",water_level=5.0 1440046805")
                .collect(Collectors.joining("\\n"));

        Assertions.assertThat(pointsBody()).isEqualTo(expectedContent);

        // there is no exception
        verifier.verify();
    }

    @Test
    void flushByDuration() {

        // after 5 actions or 1000 seconds
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(5)
                .flushInterval(1_000_000)
                .build();

        setUp(batchOptions);

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDBReactive.writeMeasurements(measurements);

        // without call remote api
        Mockito.verify(influxDBService, Mockito.never()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // 1_000_000 milliseconds to feature
        batchScheduler.advanceTimeBy(1_000_000, TimeUnit.MILLISECONDS);

        // was call remote API
        Mockito.verify(influxDBService, Mockito.only()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // check content
        Assertions.assertThat(requestBody.getAllValues().size()).isEqualTo(1);

        String expectedContent = Stream.of
                ("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 1440046802",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 1440046803",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 1440046804")
                .collect(Collectors.joining("\\n"));

        Assertions.assertThat(pointsBody()).isEqualTo(expectedContent);

        // there is no exception
        verifier.verify();
    }

    /**
     * @see Flowable#timeout(long, TimeUnit)
     */
    @Test
    void jitterJitterInterval() {

        // after 5 actions or 10 seconds + 5 seconds jitter interval
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(5)
                .flushInterval(10_000)
                .jitterInterval(5_000)
                .build();

        setUp(batchOptions);

        // publish measurement
        influxDBReactive.writeMeasurement(createMeasurement(150));

        // move time to feature by 10 seconds - flush interval elapsed
        batchScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        // without call remote api
        Mockito.verify(influxDBService, Mockito.never()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // move time to feature by 5 seconds - jitter interval elapsed
        jitterScheduler.advanceTimeBy(5, TimeUnit.SECONDS);

        // was call remote API
        Mockito.verify(influxDBService, Mockito.only()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // check content
        Assertions.assertThat(requestBody.getAllValues().size()).isEqualTo(1);

        Assertions
                .assertThat(pointsBody())
                .isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 150\",water_level=150.0 1440046950");

        // there is no exception
        verifier.verify();
    }

    @Test
    void flushBeforeClose() {

        // after 5 actions
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled().actions(5).build();
        setUp(batchOptions);

        influxDBReactive.writeMeasurement(createMeasurement(1));

        // only 1 action
        Mockito.verify(influxDBService, Mockito.never()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // close InfluxDBReactive
        influxDBReactive.close();

        // was call remote API
        Mockito.verify(influxDBService, Mockito.only()).writePoints(
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any(),
                Mockito.any());

        // check content
        Assertions.assertThat(requestBody.getAllValues().size()).isEqualTo(1);

        Assertions
                .assertThat(pointsBody())
                .isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801");

        // there is no exception
        verifier.verify();
    }

    @Nonnull
    private H2OFeetMeasurement createMeasurement(@Nonnull final Integer index) {

        Objects.requireNonNull(index, "Measurement index is required");

        double level = index.doubleValue();
        long time = 1440046800L + index;

        return new H2OFeetMeasurement("coyote_creek", level, "feet " + index, time);
    }
}
