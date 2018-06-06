package org.influxdb.impl;

import org.assertj.core.api.Assertions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakub Bednar (bednar@github) (05/06/2018 15:46)
 */
public class InfluxDBReactiveListenerVerifier extends InfluxDBReactiveListenerDefault {

    private List<Throwable> throwables = new ArrayList<>();

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
}