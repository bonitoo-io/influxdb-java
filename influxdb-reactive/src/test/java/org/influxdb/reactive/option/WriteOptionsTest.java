package org.influxdb.reactive.option;

import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDB;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (14/06/2018 15:56)
 */
@RunWith(JUnitPlatform.class)
class WriteOptionsTest {

    @Test
    void defaults() {

        WriteOptions writeOptions = WriteOptions.builder().database("my_db").build();

        Assertions.assertThat(writeOptions.getDatabase()).isEqualTo("my_db");
        Assertions.assertThat(writeOptions.getRetentionPolicy()).isEqualTo("autogen");
        Assertions.assertThat(writeOptions.getConsistencyLevel()).isEqualTo(InfluxDB.ConsistencyLevel.ONE);
        Assertions.assertThat(writeOptions.getPrecision()).isEqualTo(TimeUnit.NANOSECONDS);
    }

    @Test
    void databaseRequired() {

        WriteOptions.Builder builder = WriteOptions.builder();

        Assertions.assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for database");
    }

    @Test
    void retentionPolicy() {

        WriteOptions writeOptions = WriteOptions.builder().database("my_db").retentionPolicy("my_policy").build();

        Assertions.assertThat(writeOptions.getRetentionPolicy()).isEqualTo("my_policy");
    }

    @Test
    void retentionPolicyRequired() {

        WriteOptions.Builder builder = WriteOptions.builder().database("my_db");

        Assertions.assertThatThrownBy(() -> builder.retentionPolicy(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a non-empty string for retentionPolicy");
    }

    @Test
    void consistencyLevel() {

        WriteOptions writeOptions = WriteOptions.builder().database("my_db")
                .consistencyLevel(InfluxDB.ConsistencyLevel.QUORUM).build();

        Assertions.assertThat(writeOptions.getConsistencyLevel()).isEqualTo(InfluxDB.ConsistencyLevel.QUORUM);
    }

    @Test
    void consistencyLevelRequired() {

        WriteOptions.Builder builder = WriteOptions.builder().database("my_db");

        Assertions.assertThatThrownBy(() -> builder.consistencyLevel(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("InfluxDB.ConsistencyLevel is required");
    }

    @Test
    void precision() {

        WriteOptions writeOptions = WriteOptions.builder().database("my_db")
                .precision(TimeUnit.HOURS).build();

        Assertions.assertThat(writeOptions.getPrecision()).isEqualTo(TimeUnit.HOURS);
    }

    @Test
    void precisionRequired() {

        WriteOptions.Builder builder = WriteOptions.builder().database("my_db");

        Assertions.assertThatThrownBy(() -> builder.precision(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("TimeUnit precision is required");
    }
}