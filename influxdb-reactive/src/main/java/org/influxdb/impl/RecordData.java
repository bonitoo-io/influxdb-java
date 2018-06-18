package org.influxdb.impl;

import org.influxdb.reactive.option.WriteOptions;

import javax.annotation.Nonnull;

/**
 * @author Jakub Bednar (bednar@github) (18/06/2018 14:57)
 */
final class RecordData extends AbstractData<String> {
    private final String record;

    RecordData(@Nonnull final String record, @Nonnull final WriteOptions writeOptions) {
        super(writeOptions);
        this.record = record;
    }

    @Nonnull
    @Override
    String getData() {
        return record;
    }

    @Nonnull
    @Override
    String lineProtocol() {
        return record;
    }
}
