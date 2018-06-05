package org.influxdb.reactive;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 09:09)
 */
class BatchOptionsReactiveTest {

    @Test
    void defaultScheduler() {

        BatchOptionsReactive batchOptions = BatchOptionsReactive.builder().build();

        Assertions.assertThat(batchOptions.getBatchingScheduler()).isEqualTo(Schedulers.computation());
    }

    @Test
    void configureScheduler() {

        TestScheduler scheduler = new TestScheduler();

        BatchOptionsReactive batchOptions = BatchOptionsReactive.builder()
                .batchingScheduler(scheduler)
                .build();

        Assertions.assertThat(batchOptions.getBatchingScheduler()).isEqualTo(scheduler);
    }
}