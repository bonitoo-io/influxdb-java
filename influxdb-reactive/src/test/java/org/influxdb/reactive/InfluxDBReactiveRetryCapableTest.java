package org.influxdb.reactive;

import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (07/06/2018 23:15)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveRetryCapableTest extends AbstractInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {
        super.setUp(BatchOptionsReactive.DISABLED);
    }

    @Test
    void writeAfterRetryException() {

        // First Retry Error than Success
        influxDBServer.enqueue(new MockResponse().setResponseCode(400)
                .addHeader("X-Influxdb-Error", "cache-max-memory-size exceeded 104/1400"));

        influxDBServer.enqueue(new MockResponse());


        H2OFeetMeasurement measurement = new H2OFeetMeasurement(
                "coyote_creek", 2.927, "below 3 feet", 1440046800L);

        influxDBReactive.writeMeasurement(measurement);

        verifier.verifyErrorResponse(1);
        verifier.verifySuccessResponse(1);

        Assertions.assertThat(influxDBServer.getRequestCount())
                .isEqualTo(2);

        String expected = "h2o_feet,location=coyote_creek " +
                "level\\ description=\"below 3 feet\",water_level=2.927 1440046800";
        
        Assertions.assertThat(pointsBody()).isEqualTo(expected);
        Assertions.assertThat(pointsBody()).isEqualTo(expected);
    }
}
