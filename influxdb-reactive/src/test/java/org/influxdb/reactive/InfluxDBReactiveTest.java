package org.influxdb.reactive;

import org.assertj.core.api.Assertions;
import org.influxdb.impl.AbstractInfluxDBReactiveTest;
import org.junit.Assert;
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
        super.setUp(BatchOptionsReactive.DISABLED);
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
}
