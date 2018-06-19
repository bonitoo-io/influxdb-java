package org.influxdb.reactive;

import io.reactivex.Flowable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 07:04)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveWriteBatchingTest extends AbstractInfluxDBReactiveTest {

    @Test
    void flushByActions() {

        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(5)
                .flushInterval(1_000_000)
                .writeScheduler(Schedulers.trampoline())
                .build();

        setUp(batchOptions);

        influxDBServer.enqueue(new MockResponse());

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDBReactive.writeMeasurements(measurements);

        // only 4 actions
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(0);

        // the fifth action => store to InfluxDB
        influxDBReactive.writeMeasurement(createMeasurement(5));

        // was call remote API
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // check content
        String expectedContent = Stream.of
                ("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 1440046802",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 1440046803",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 1440046804",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 5\",water_level=5.0 1440046805")
                .collect(Collectors.joining("\n"));

        Assertions.assertThat(pointsBody()).isEqualTo(expectedContent);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void flushByDuration() {

        // after 10 actions or 1000 seconds
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(10)
                .flushInterval(1_000_000)
                .writeScheduler(Schedulers.trampoline())
                .build();

        setUp(batchOptions);

        influxDBServer.enqueue(new MockResponse());

        Flowable<H2OFeetMeasurement> measurements = Flowable.just(
                createMeasurement(1),
                createMeasurement(2),
                createMeasurement(3),
                createMeasurement(4));

        influxDBReactive.writeMeasurements(measurements);

        // the fifth measurement
        influxDBReactive.writeMeasurement(createMeasurement(5));

        // without call remote api
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(0);

        // 1_000 seconds to feature
        advanceTimeBy(1_000, batchScheduler);

        // was call remote API
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // check content
        String expectedContent = Stream.of
                ("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 2\",water_level=2.0 1440046802",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 3\",water_level=3.0 1440046803",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 4\",water_level=4.0 1440046804",
                        "h2o_feet,location=coyote_creek level\\ description=\"feet 5\",water_level=5.0 1440046805")
                .collect(Collectors.joining("\n"));

        Assertions.assertThat(pointsBody()).isEqualTo(expectedContent);

        // there is no exception
        verifier.verifySuccess();
    }

    /**
     * @see Flowable#timeout(long, TimeUnit)
     */
    @Test
    void jitterInterval() {

        // after 5 actions or 10 seconds + 5 seconds jitter interval
        BatchOptionsReactive batchOptions = BatchOptionsReactive.disabled()
                .actions(5)
                .flushInterval(10_000)
                .jitterInterval(5_000)
                .writeScheduler(Schedulers.trampoline())
                .build();

        setUp(batchOptions);

        influxDBServer.enqueue(new MockResponse());

        // publish measurement
        influxDBReactive.writeMeasurement(createMeasurement(150));

        // move time to feature by 10 seconds - flush interval elapsed
        advanceTimeBy(10, batchScheduler);

        // without call remote api
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(0);

        // move time to feature by 5 seconds - jitter interval elapsed
        advanceTimeBy(6, jitterScheduler);

        // was call remote API
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // check content

        String expected =
                "h2o_feet,location=coyote_creek level\\ description=\"feet 150\",water_level=150.0 1440046950";

        Assertions
                .assertThat(pointsBody())
                .isEqualTo(expected);

        // there is no exception
        verifier.verifySuccess();
    }

    @Test
    void flushBeforeClose() {

        // after 5 actions
        BatchOptionsReactive batchOptions = BatchOptionsReactive
                .disabled()
                .actions(5)
                .writeScheduler(Schedulers.trampoline())
                .build();

        setUp(batchOptions);

        influxDBServer.enqueue(new MockResponse());

        influxDBReactive.writeMeasurement(createMeasurement(1));

        // only 1 action
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(0);

        // close InfluxDBReactive
        influxDBReactive.close();

        // was call remote API
        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);

        // check content

        Assertions
                .assertThat(pointsBody())
                .isEqualTo("h2o_feet,location=coyote_creek level\\ description=\"feet 1\",water_level=1.0 1440046801");

        // there is no exception
        verifier.verifySuccess();
    }

    @Nonnull
    private H2OFeetMeasurement createMeasurement(@Nonnull final Integer index) {

        return H2OFeetMeasurement.createMeasurement(index);
    }
}
