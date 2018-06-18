package org.influxdb.impl;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;
import java.util.Objects;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 14:57)
 */
abstract class AbstractData<D> {

    private WriteOptions writeOptions;

    AbstractData(@Nonnull final WriteOptions writeOptions) {

        Objects.requireNonNull(writeOptions, "WriteOptions are required");

        this.writeOptions = writeOptions;
    }

    @Nonnull
    WriteOptions getWriteOptions() {
        return writeOptions;
    }

    @Nonnull
    abstract D getData();

    @Nonnull
    abstract String lineProtocol();
}
