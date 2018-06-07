package org.influxdb.impl;

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

    private final LongAdder responses = new LongAdder();
    private List<Throwable> throwables = new ArrayList<>();

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

    public void verify()
    {
        Assertions
                .assertThat(throwables.size())
                .withFailMessage("Unexpected exceptions: %s", throwables)
                .isEqualTo(0);
    }

    public void verifyErrors(final int expected)
    {
        Assertions.assertThat(throwables.size())
                .isEqualTo(expected);
    }

    public void waitForResponse(@Nonnull final Long responseCount)
    {
        Objects.requireNonNull(responseCount, "Response count is required");

        LOG.log(Level.FINEST, "Wait for responses: {0}", responseCount);

        long start = System.currentTimeMillis();
        while (responseCount > responses.longValue())
        {
            if (System.currentTimeMillis() - start > 10_0000)
            {
                throw new RuntimeException("Response did not arrived in 10 seconds.");
            }
        }

        LOG.log(Level.FINEST, "Responses arrived");
    }
}