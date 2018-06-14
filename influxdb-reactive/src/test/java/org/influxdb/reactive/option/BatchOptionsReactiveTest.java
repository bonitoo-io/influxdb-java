package org.influxdb.reactive.option;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:09)
 */
@RunWith(JUnitPlatform.class)
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