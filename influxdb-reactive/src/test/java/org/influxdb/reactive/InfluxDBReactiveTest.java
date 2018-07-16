package org.influxdb.reactive;

import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.influxdb.reactive.options.BatchOptionsReactive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (14/06/2018 12:04)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveTest extends AbstractInfluxDBReactiveTest {

    @BeforeEach
    void setUp() {
        super.setUp(BatchOptionsReactive.DEFAULTS);
    }

    @Test
    void close() {

        Assertions.assertThat(influxDBReactive.isClosed()).isEqualTo(false);

        influxDBReactive.close();

        Assertions.assertThat(influxDBReactive.isClosed()).isEqualTo(true);
    }

    @Test
    void gzip() {

        // default disabled
        Assertions.assertThat(influxDBReactive.isGzipEnabled()).isEqualTo(false);

        // enable
        influxDBReactive.enableGzip();
        Assertions.assertThat(influxDBReactive.isGzipEnabled()).isEqualTo(true);

        // disable
        influxDBReactive.disableGzip();
        Assertions.assertThat(influxDBReactive.isGzipEnabled()).isEqualTo(false);
    }

    @Test
    void ping() {

        influxDBServer.enqueue(new MockResponse().setHeader("X-Influxdb-Version", "v1.5.2"));

        influxDBReactive
                .ping()
                .test()
                .assertValueCount(1)
                .assertValue(pong -> {

                    Assertions.assertThat(pong.getVersion()).isEqualTo("v1.5.2");
                    Assertions.assertThat(pong.isGood()).isTrue();
                    Assertions.assertThat(pong.getResponseTime()).isGreaterThan(0);

                    return true;
                });
    }

    @Test
    void version() {

        influxDBServer.enqueue(new MockResponse().setHeader("X-Influxdb-Version", "v1.5.2"));

        influxDBReactive
                .version()
                .test()
                .assertValueCount(1)
                .assertValue("v1.5.2");
    }
}
