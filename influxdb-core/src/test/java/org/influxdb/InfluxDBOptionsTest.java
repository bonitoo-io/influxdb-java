package org.influxdb;

import okhttp3.OkHttpClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (01/06/2018 08:18)
 */
@RunWith(JUnitPlatform.class)
class InfluxDBOptionsTest {

    @Test
    void optionsDefault()
    {
        InfluxDBOptions options = InfluxDBOptions.builder().url("http://influxdb:8086").build();

        Assertions.assertEquals("http://influxdb:8086", options.getUrl());
        Assertions.assertNull(options.getPassword());
        Assertions.assertNull(options.getUsername());
        Assertions.assertNotNull(options.getOkHttpClient());
    }

    @Test
    void optionsFull() {

        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

        InfluxDBOptions options = InfluxDBOptions.builder()
                .url("http://influxdb:8086")
                .username("admin")
                .password("password")
                .okHttpClient(okBuilder)
                .build();

        Assertions.assertEquals("http://influxdb:8086", options.getUrl());
        Assertions.assertEquals("admin", options.getUsername());
        Assertions.assertEquals("password", options.getPassword());
        Assertions.assertEquals(okBuilder, options.getOkHttpClient());
    }

    @Test
    void urlIsNotEmptyString() {

        InfluxDBOptions.Builder builder = InfluxDBOptions.builder();

        Assertions.assertThrows(IllegalArgumentException.class, () -> builder.url(""));
    }

    @Test
    void urlIsRequired() {

        InfluxDBOptions.Builder builder = InfluxDBOptions.builder();

        Assertions.assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void okHttpClientIsRequired() {

        InfluxDBOptions.Builder builder = InfluxDBOptions.builder().url("http://influxdb:8086");

        //noinspection ConstantConditions
        Assertions.assertThrows(NullPointerException.class, () -> builder.okHttpClient(null));
    }
}