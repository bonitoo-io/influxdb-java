package org.influxdb.reactive;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:09)
 */
class BatchOptionsReactiveTest {

    @Test
    void defaults() {

        BatchOptionsReactive batchOptions = BatchOptionsReactive.builder().build();

        Assertions.assertThat(batchOptions.getActions()).isEqualTo(1000);
        Assertions.assertThat(batchOptions.getBufferLimit()).isEqualTo(10000);
        Assertions.assertThat(batchOptions.getFlushInterval()).isEqualTo(1000);
        Assertions.assertThat(batchOptions.getJitterInterval()).isEqualTo(0);
    }
}