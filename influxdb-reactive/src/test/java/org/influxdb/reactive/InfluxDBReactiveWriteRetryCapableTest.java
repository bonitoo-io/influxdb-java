package org.influxdb.reactive;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (07/06/2018 23:15)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveWriteRetryCapableTest extends AbstractInfluxDBReactiveTest {

    @Test
    void writeAfterRetryException() {

        setUp(BatchOptionsReactive.DISABLED);

        // First Retry Error than Success
        influxDBServer.enqueue(createErrorResponse("cache-max-memory-size exceeded 104/1400"));
        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        influxDBReactive.writeMeasurement(measurement);

        advanceTimeBy(5, retryScheduler);

        verifier.verifyErrorResponse(1);
        verifier.verifySuccessResponse(1);

        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(2);
    }

    @Test
    void withoutRetry() {

        setUp(BatchOptionsReactive.DISABLED);

        // Only error Retry Error than Success
        influxDBServer.enqueue(createErrorResponse("database not found: not_exist_database"));


        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440046855L);

        influxDBReactive.writeMeasurement(measurement);

        advanceTimeBy(5, retryScheduler);

        verifier.verifyErrorResponse(1);
        verifier.verifySuccessResponse(0);

        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(1);
    }

    @Test
    void order() {

        // random retry interval
        BatchOptionsReactive options = BatchOptionsReactive
                .disabled()
                .retryInterval(500)
                .jitterInterval(500)
                .build();

        // use Trampoline scheduler => retry wait on main thread
        setUp(options, new TestScheduler(), Schedulers.trampoline(), Schedulers.trampoline());

        // success, fail, fail, success, success
        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(createErrorResponse("cache-max-memory-size exceeded 104/1400"));
        influxDBServer.enqueue(createErrorResponse("cache-max-memory-size exceeded 104/1400"));
        influxDBServer.enqueue(new MockResponse());
        influxDBServer.enqueue(new MockResponse());

        H2OFeetMeasurement measurement1 = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046801L);

        H2OFeetMeasurement measurement2 = new H2OFeetMeasurement(
                "coyote_creek", 1.927, "below 2 feet", 1440049802L);

        H2OFeetMeasurement measurement3 = new H2OFeetMeasurement(
                "coyote_creek", 5.927, "over 5 feet", 1440052803L);

        List<H2OFeetMeasurement> measurements = new ArrayList<>();
        measurements.add(measurement1);
        measurements.add(measurement2);
        measurements.add(measurement3);

        influxDBReactive.writeMeasurements(measurements);

        // wait for retry
        verifier.waitForResponse(5);
        verifier.verifyErrorResponse(2);
        verifier.verifySuccessResponse(3);

        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(5);

        String measurement1Expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046801";

        String measurement2Expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 2 feet\",water_level=1.927 1440049802";

        String measurement3Expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"over 5 feet\",water_level=5.927 1440052803";

        // success 1
        Assertions.assertThat(pointsBody()).isEqualTo(measurement1Expected);

        // fail 2
        Assertions.assertThat(pointsBody()).isEqualTo(measurement2Expected);
        // fail 2
        Assertions.assertThat(pointsBody()).isEqualTo(measurement2Expected);
        // success 2
        Assertions.assertThat(pointsBody()).isEqualTo(measurement2Expected);

        // success 3
        Assertions.assertThat(pointsBody()).isEqualTo(measurement3Expected);
    }
}
