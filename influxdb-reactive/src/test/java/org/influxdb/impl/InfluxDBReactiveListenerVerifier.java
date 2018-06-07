package org.influxdb.impl;

import io.reactivex.disposables.Disposable;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDBException;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:46)
 */
public class InfluxDBReactiveListenerVerifier extends InfluxDBReactiveListenerDefault {

    private static final Logger LOG = Logger.getLogger(InfluxDBReactiveListenerVerifier.class.getName());

    private Disposable writeDisposable;

    private LongAdder backpressures = new LongAdder();
    private LongAdder responses = new LongAdder();
    private List<Throwable> throwables = new ArrayList<>();

    @Override
    public void doOnSubscribeWriter(@Nonnull Disposable writeDisposable) {
        super.doOnSubscribeWriter(writeDisposable);

        this.writeDisposable = writeDisposable;
    }

    @Override
    public void doOnSuccessResponse() {
        super.doOnSuccessResponse();

        responses.add(1);
    }

    @Override
    public void doOnErrorResponse(@Nonnull final InfluxDBException throwable) {
        super.doOnErrorResponse(throwable);

        throwables.add(throwable);

        responses.add(1);
    }

    @Override
    public void doOnError(@Nonnull Throwable throwable) {
        super.doOnError(throwable);

        throwables.add(throwable);
    }

    @Override
    public void doOnBackpressure() {
        super.doOnBackpressure();

        backpressures.add(1);
    }

    public void verify() {
        Assertions
                .assertThat(throwables.size())
                .withFailMessage("Unexpected exceptions: %s", throwables)
                .isEqualTo(0);
    }

    public void verifyErrors(final int expected) {
        Assertions.assertThat(throwables.size())
                .isEqualTo(expected);
    }

    /**
     * @return the count Backpressure event
     */
    @Nonnull
    public Long verifyBackpressure() {
        Assertions
                .assertThat(backpressures.longValue())
                .withFailMessage("Backpressure wasn't applied")
                .isGreaterThan(0);

        return backpressures.longValue();
    }

    public void waitForResponse(@Nonnull final Long responseCount) {
        Objects.requireNonNull(responseCount, "Response count is required");

        LOG.log(Level.FINEST, "Wait for responses: {0}", responseCount);

        long start = System.currentTimeMillis();
        while (responseCount > responses.longValue()) {
            if (System.currentTimeMillis() - start > 10_000) {
                throw new RuntimeException("Response did not arrived in 10 seconds.");
            }
        }

        LOG.log(Level.FINEST, "Responses arrived");
    }

    public void waitForWriteDisposed() {

        long start = System.currentTimeMillis();

        while (!writeDisposable.isDisposed()) {
            if (System.currentTimeMillis() - start > 10_000) {
                throw new RuntimeException("Writer did not disposed in 10 seconds.");
            }
        }
    }
}