package org.influxdb.impl;

import org.assertj.core.api.Assertions;
import org.influxdb.reactive.InfluxDBReactiveListener;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:46)
 */
public class InfluxDBReactiveListenerVerifier implements InfluxDBReactiveListener {

    private List<Throwable> throwables = new ArrayList<>();

    @Override
    public void doOnError(@Nonnull Throwable throwable) {

        throwables.add(throwable);
    }

    public void verify()
    {
        Assertions
                .assertThat(throwables.size())
                .withFailMessage("Unexpected exceptions: %s", throwables)
                .isEqualTo(0);
    }
}