package org.influxdb.flux.options.query;

import org.influxdb.impl.Preconditions;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;
import java.util.StringJoiner;

/**
 * @author Jakub Bednar (bednar@github) (26/07/2018 12:35)
 * @since 3.0.0
 */
@ThreadSafe
abstract class AbstractOption {

    private final String name;
    protected String value;

    AbstractOption(@Nonnull final String name) {
        Preconditions.checkNonEmptyString(name, "Name of option");

        this.name = name;
    }

    @Override
    public String toString() {

        return new StringJoiner(" ")
                .add("option")
                .add(name)
                .add("=")
                .add(value)
                .toString();
    }
}
