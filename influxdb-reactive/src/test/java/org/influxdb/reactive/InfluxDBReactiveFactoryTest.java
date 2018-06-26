package org.influxdb.reactive;

import org.assertj.core.api.Assertions;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDBOptions;
import org.influxdb.reactive.option.BatchOptionsReactive;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (12/06/2018 12:54)
 * @since 3.0.0
 */
@RunWith(JUnitPlatform.class)
class InfluxDBReactiveFactoryTest {

    @Test
    void optionsRequired() {

        Assertions.assertThatThrownBy(() -> InfluxDBReactiveFactory.connect(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("InfluxDBOptions is required");
    }

    @Test
    void batchOptionsRequired() {

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://172.17.0.2:8086")
                .username("root")
                .password("root")
                .database("reactive_measurements")
                .build();

        Assertions.assertThatThrownBy(() -> InfluxDBReactiveFactory.connect(options, (BatchOptionsReactive) null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("BatchOptionsReactive is required");
    }
}