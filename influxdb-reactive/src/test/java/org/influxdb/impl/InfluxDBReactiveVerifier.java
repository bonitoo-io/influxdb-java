package org.influxdb.impl;

import org.assertj.core.api.Assertions;
import org.influxdb.reactive.InfluxDBReactive;
import org.influxdb.reactive.events.BackpressureEvent;
import org.influxdb.reactive.events.QueryParsedResponseEvent;
import org.influxdb.reactive.events.UnhandledErrorEvent;
import org.influxdb.reactive.events.WriteErrorEvent;
import org.influxdb.reactive.events.WritePartialEvent;
import org.influxdb.reactive.events.WriteSuccessEvent;
import org.influxdb.reactive.events.WriteUDPEvent;

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
public class InfluxDBReactiveVerifier {

    private static final Logger LOG = Logger.getLogger(InfluxDBReactiveVerifier.class.getName());

    private LongAdder backpressures = new LongAdder();
    private LongAdder successResponses = new LongAdder();
    private LongAdder errorResponses = new LongAdder();
    private LongAdder responseMapperCallCount = new LongAdder();
    private List<Throwable> throwables = new ArrayList<>();

    private InfluxDBReactive influxDBReactive;

    InfluxDBReactiveVerifier(@Nonnull final InfluxDBReactive influxDBReactive) {

        Objects.requireNonNull(influxDBReactive, "InfluxDBReactive is required");

        // WriteSuccessEvent
        influxDBReactive
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> successResponses.add(1));

        // WriteErrorEvent
        influxDBReactive
                .listenEvents(WriteErrorEvent.class)
                .subscribe(event -> {
                    throwables.add(event.getException());
                    errorResponses.add(1);
                });

        // WritePartialEvent
        influxDBReactive
                .listenEvents(WritePartialEvent.class)
                .subscribe(event -> {
                    throwables.add(event.getException());
                    errorResponses.add(1);
                });

        // WriteUDPEvent
        influxDBReactive
                .listenEvents(WriteUDPEvent.class)
                .subscribe(event -> successResponses.add(1));

        // UnhandledErrorEvent
        influxDBReactive
                .listenEvents(UnhandledErrorEvent.class)
                .subscribe(event -> throwables.add(event.getThrowable()));

        // QueryParsedResponseEvent
        influxDBReactive
                .listenEvents(QueryParsedResponseEvent.class)
                .subscribe(event -> responseMapperCallCount.add(1));

        // BackpressureEvent
        influxDBReactive.listenEvents(BackpressureEvent.class)
                .subscribe(event -> backpressures.add(1));

        this.influxDBReactive = influxDBReactive;
    }

    public void verifySuccess() {
        Assertions
                .assertThat(throwables.size())
                .withFailMessage("Unexpected exceptions: %s", throwables)
                .isEqualTo(0);
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

    public void verifyNoBackpressure() {
        Assertions
                .assertThat(backpressures.longValue())
                .withFailMessage("Backpressure was applied")
                .isEqualTo(0);
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

    public void waitForClose() {

        long start = System.currentTimeMillis();

        while (!influxDBReactive.isClosed()) {
            if (System.currentTimeMillis() - start > 10_000) {
                throw new RuntimeException("Writer did not disposed in 10 seconds.");
            }
        }
    }

    void reset() {

        backpressures.reset();
        successResponses.reset();
        errorResponses.reset();
        responseMapperCallCount.reset();
        throwables.clear();
    }
}