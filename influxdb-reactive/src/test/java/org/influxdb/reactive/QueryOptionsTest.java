package org.influxdb.reactive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (11/06/2018 14:13)
 */
@RunWith(JUnitPlatform.class)
class QueryOptionsTest {

    @Test
    void defaults() {
        QueryOptions queryOptions = QueryOptions.builder().build();

        Assertions.assertThat(queryOptions.getChunkSize()).isEqualTo(10_000);
    }

    @Test
    void chunkSizePositive() {

        QueryOptions.Builder queryOptions = QueryOptions.builder();

        Assertions.assertThatThrownBy(() -> queryOptions.chunkSize(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expecting a positive number for chunkSize");
    }
}
