package org.influxdb.impl;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Jakub Bednar (bednar@github) (31/07/2018 10:21)
 */
abstract class AbstractTest {

    protected CountDownLatch countDownLatch;

    @BeforeEach
    protected void prepare() {
        countDownLatch = new CountDownLatch(1);
    }

    protected void waitToCallback() {
        try {
            Assertions.assertThat(countDownLatch.await(10, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Assertions.fail("Unexpected exception", e);
        }
    }
}