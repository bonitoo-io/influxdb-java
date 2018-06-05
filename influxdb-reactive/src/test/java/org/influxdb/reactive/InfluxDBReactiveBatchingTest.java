package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.schedulers.TestScheduler;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
class InfluxDBReactiveBatchingTest extends AbstractInfluxDBReactiveTest {

    @Test
    void flushByActions() {

        // after 5 actions
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled().actions(5).flushDuration(1_000_000).build();
        setUp(batchOptions);

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDB.writeMeasurements(measurements);

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
        influxDB.writeMeasurement(createMeasurement(5));

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
    }

    @Test
    void flushByDuration() {

        TestScheduler scheduler = new TestScheduler();

        // after 5 actions or 1000 seconds
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(5)
                .flushDuration(1_000_000)
                .batchingScheduler(scheduler)
                .build();

        setUp(batchOptions);

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDB.writeMeasurements(measurements);

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
        scheduler.advanceTimeBy(1_000_000, TimeUnit.MILLISECONDS);

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
    }

    @Test
    void flushBeforeClose() {

        // after 5 actions
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled().actions(5).build();
        setUp(batchOptions);

        influxDB.writeMeasurement(createMeasurement(1));

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
        influxDB.close();

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
    }

    @Nonnull
    private H2OFeetMeasurement createMeasurement(@Nonnull final Integer index) {

        Objects.requireNonNull(index, "Measurement index is required");

        double level = index.doubleValue();
        long time = 1440046800L + index;

        return new H2OFeetMeasurement("coyote_creek", level, "feet " + index, time);
    }
}
