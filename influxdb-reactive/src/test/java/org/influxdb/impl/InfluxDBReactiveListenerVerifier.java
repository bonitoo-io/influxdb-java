package org.influxdb.impl;

import io.reactivex.disposables.Disposable;
import org.assertj.core.api.Assertions;
import org.influxdb.InfluxDBException;
import org.influxdb.reactive.InfluxDBReactiveListenerDefault;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
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
    private LongAdder successResponses = new LongAdder();
    private LongAdder errorResponses = new LongAdder();
    private LongAdder responseMapperCallCount = new LongAdder();
    private List<Throwable> throwables = new ArrayList<>();

    @Override
    public void doOnSubscribeWriter(@Nonnull Disposable writeDisposable) {
        super.doOnSubscribeWriter(writeDisposable);

        this.writeDisposable = writeDisposable;
    }

    @Override
    public void doOnSuccessResponse() {
        super.doOnSuccessResponse();

        successResponses.add(1);
    }

    @Override
    public void doOnErrorResponse(@Nonnull final InfluxDBException throwable) {
        super.doOnErrorResponse(throwable);

        throwables.add(throwable);

        errorResponses.add(1);
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

    @Override
    public void doOnQueryResult() {
        super.doOnQueryResult();

        responseMapperCallCount.add(1);
    }

    public void verifySuccess() {
        Assertions
                .assertThat(throwables.size())
                .withFailMessage("Unexpected exceptions: %s", throwables)
                .isEqualTo(0);
    }

    public void verifyError(final int index, @Nonnull final Class<? extends Throwable> typeOfException) {

        Assertions
                .assertThat(throwables.get(index))
                .isInstanceOf(typeOfException);
    }


    public void verifyErrorResponse(final int expected) {
        Assertions.assertThat(errorResponses.longValue())
                .isEqualTo(expected);
    }

    public void verifySuccessResponse(final int expected) {
        Assertions.assertThat(successResponses.longValue())
                .isEqualTo(expected);
    }

    public void verifyResponseMapperCalls(final int expected) {
        Assertions.assertThat(responseMapperCallCount.longValue())
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

    public void waitForResponse(final int responseCount) {

        LOG.log(Level.FINEST, "Wait for responses: {0}", responseCount);

        long start = System.currentTimeMillis();
        while (responseCount > (successResponses.longValue() + errorResponses.longValue())) {
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